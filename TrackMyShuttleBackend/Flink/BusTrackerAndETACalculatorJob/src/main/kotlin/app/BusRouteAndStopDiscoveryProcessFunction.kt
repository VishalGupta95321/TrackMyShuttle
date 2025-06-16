package app

import models.*
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import util.RouteType
import util.TimeStamp
import util.getListState
import util.getValueState

/// It finds out in which DIRECTION bus is heading FROM which STOP - TO which STOP, on which ROUTE and if it reached its DESTINATION.
class BusRouteAndStopDiscoveryProcessFunction :
    KeyedProcessFunction<String, EnrichedLocationData, BusLocationWithMetadata>() {

    private lateinit var busDiscovery: BusRouteAndStopDiscoverer

    /// Need to find out these values for output stream.
    private lateinit var recentCoordinates: ListState<TimeStampedCoordinate>
    private lateinit var lastPassedBusStop: ValueState<Pair<TimeStamp?, BusStop>>
    private lateinit var nextBusStop: ValueState<BusStop>
    private lateinit var currentStop: ValueState<BusStop>
    private lateinit var isReturning: ValueState<Boolean>


    override fun open(openContext: OpenContext?) {

        busDiscovery = BusRouteAndStopDiscoverer()

        runtimeContext.apply {
            recentCoordinates = getListState<TimeStampedCoordinate>(RECENT_COORDINATES)
            lastPassedBusStop = getValueState<Pair<TimeStamp?, BusStop>>(LAST_PASSED_BUS_STOP)
            nextBusStop = getValueState<BusStop>(NEXT_BUS_STOP)
            currentStop = getValueState<BusStop>(CURRENT_STOP)
            isReturning = getValueState<Boolean>(IS_RETURNING)
        }
    }

    override fun processElement(
        element: EnrichedLocationData,
        context: KeyedProcessFunction<String, EnrichedLocationData, BusLocationWithMetadata>.Context,
        out: Collector<BusLocationWithMetadata>
    ) {
        val busStops = element.busStopsData
        val busRoutes = element.routesData
        val busRouteType = element.busData.routeType


        /// Discovering Bus MetaData if not available already.
        if (lastPassedBusStop.value() == null || nextBusStop.value() == null || isReturning.value() == null) {
            findNextLastStopAndIfIsReturningFromScratch(busStops, busRoutes, busRouteType, element.locationData)
            return
        }

        /// Checking If Bus reached the destination stop and adjusting the metadata accordingly.
        checkIfBusReachedDestAndUpdateMetadata(busRouteType, busStops, element.locationData)

        /// Getting a POINT from available routes between set of stops.
        if (lastPassedBusStop.value() != null || nextBusStop.value() != null || isReturning.value() != null)
            getPointInRouteFromAvailableRoutesAndCollectAndUpdateMetadata(
                routes = busRoutes,
                busLocation = element.locationData,
            ) { point, currentRoute ->
                out.collect(
                    BusLocationWithMetadata(
                        busId = context.currentKey,
                        currentRoute = currentRoute,
                        routeType = busRouteType,
                        location = TimeStampedCoordinate(
                            timestamp = element.locationData.timestamp,
                            coordinate = point
                        ),
                        isReturning = isReturning.value(),
                        currentStop = currentStop.value(),
                        nextStop = nextBusStop.value(),
                        lastPassedStop = lastPassedBusStop.value(),
                    )
                )
            }
    }


    private fun getPointInRouteFromAvailableRoutesAndCollectAndUpdateMetadata(
        routes: List<Route>,
        busLocation: BusLocationData,
        collect: (point: Coordinate, currRoute: Route) -> Unit
    ) {
        busDiscovery.getPointInRoute(
            currentPoint = busLocation.coordinate,
            lastPassedStopId = lastPassedBusStop.value().second.stopId,
            nextStopId = nextBusStop.value().stopId,
            busRoutes = routes,
        ).let { pointInRoute ->
            pointInRoute?.let {
                val currRoute = routes.find { route -> it.routeId == route.routeId }!!
                collect(it.coordinate, currRoute)
            }

            /* In case bus was not running on the available routes, maybe it is running on some other route
             between the same set of stops, but we don't have that route saved yet. Later this can be solved by calling
             MapBox directions Api but for NOW let's assume its on another route between different set of stops.*/

            /* There could be also the case that bus went offline and came online on the same route but different
            direction, in that case it will reach the wrong stop and solution of that check line no. 148*/

            pointInRoute ?: busDiscovery.getPointInRouteFromScratch(
                busLocation.coordinate, routes
            ).let { pointInRoute ->
                val isRunningBetweenDiffPoints = pointInRoute != null
                /* In case bus is running on different set of stops, different route, we are clearing all the stale metadata so
                 it had to calculate fresh again. */
                if (isRunningBetweenDiffPoints) cleaMetaData()
                /* TODO("What if bus is not running between any set of Stops ?") */
            }
        }
    }

    private fun checkIfBusReachedDestAndUpdateMetadata(
        routeType: RouteType,
        stops: List<BusStop>,
        busLocation: BusLocationData
    ) {
        stops.forEach { stop ->
            val hasReachedStop = busDiscovery.checkIfCurrentPointIsWithinBusStopRadius(
                currentPoint = busLocation.coordinate,
                stop.coordinates,
                stop.stopRadiusInMeters
            )
            /// In case bus reached the intended BusStop
            if (hasReachedStop) {
                //// We are making sure it won't update the same Current Stop again and again in case on the same stop for long.
                val currStop = currentStop.value()
                if (currStop == null || currStop.stopId != stop.stopId) {
                    // Only update the current stop if that's the stop bus was intended to reach.
                    if (stop.stopId == nextBusStop.value().stopId) {
                        currentStop.update(stop)
                        lastPassedBusStop.update(busLocation.timestamp to stop)
                        busDiscovery.getNextStopIndex(
                            currentStopIndex = stops.indexOf(stop),
                            totalBusStopsIndex = stops.size - 1,
                            routeType = routeType,
                            isReturning = isReturning.value()
                        ).let {
                            nextBusStop.update(stops[it.first])
                            isReturning.update(it.second)
                        }
                    } else {
                        /* In case bus did not reach the intended BusStop we are clearing all the stale metadata so
                          it had to calculate fresh again. */
                        cleaMetaData()
                    }
                }
            } else currentStop.clear() /// It means it's not range of any stop so we can clear the value.
        }
    }

    private fun findNextLastStopAndIfIsReturningFromScratch(
        stops: List<BusStop>,
        routes: List<Route>,
        routeType: RouteType,
        busLocation: BusLocationData
    ) {
        /// If TRUE that means bus is lost. And in if body we are discovering all the necessary metadata.
        recentCoordinates.add(
            TimeStampedCoordinate(
                timestamp = busLocation.timestamp,
                coordinate = busLocation.coordinate,
            )
        )

        busDiscovery.clearRecentCoordinatesIfFirstPointIsWithinStopRadius(
            recentCoordinates.get().map { it.toCoordinate() },
            stops,
        ).let { shouldClear -> if (shouldClear) recentCoordinates.clear() }

        busDiscovery.clearRecentCoordinatesIfIntervalPassed(
            recentCoordinates.get().toList(),
            busLocation.timestamp
        ).let { shouldClear -> if (shouldClear) recentCoordinates.clear() }

        busDiscovery.checkIfCoordinatesListCoveredTotalDistance(
            recentCoordinates.get().map { it.toCoordinate() }
        ).let { hasReachedTotalDistance ->
            if (hasReachedTotalDistance) {
                val busDiscoveryResult = busDiscovery.findNextLastStopAndIfIsReturningFromScratch(
                    routeType = routeType,
                    routes = routes,
                    busStops = stops,
                    recentCoordinates = recentCoordinates.get().map { it.toCoordinate() }
                )
                busDiscoveryResult?.let {
                    val lastPassedStop = stops[it.lastPassedStopIndex]
                    val nextStop = stops[it.nextStopIndex]
                    lastPassedBusStop.update(null to lastPassedStop)
                    nextBusStop.update(nextStop)
                    isReturning.update(it.isReturning)
                    recentCoordinates.clear()  //// Clear the list after getting the result.
                }
            }
        }
    }

    private fun cleaMetaData() {
        nextBusStop.clear()
        lastPassedBusStop.clear()
        isReturning.clear()
    }


    ///override fun close() {super.close()}

    companion object {
        private const val RECENT_COORDINATES = "RECENT_COORDINATES"
        private const val LAST_PASSED_BUS_STOP = "LAST_PASSED_BUS_STOP"
        private const val NEXT_BUS_STOP = "NEXT_BUS_STOP"
        private const val CURRENT_STOP = "CURRENT_STOP"
        private const val IS_RETURNING = "IS_RETURNING"
    }
}
