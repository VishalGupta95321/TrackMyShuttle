package app

import models.BusLocationData
import models.BusLocationWithMetadata
import models.BusStop
import models.BusData
import models.Coordinate
import models.Route
import models.TimeStampedCoordinate
import models.toCoordinate
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import util.EitherOfThree
import util.RouteType
import util.TimeStamp
import util.getListState
import util.getValueState

/// It finds out in which DIRECTION bus is heading FROM which STOP - TO which STOP, on which ROUTE and if it reached its DESTINATION.
class BusRouteAndStopDiscoveryProcessFunction :
    KeyedProcessFunction<String, EitherOfThree<BusLocationData, BusData, Route>, BusLocationWithMetadata>() {

    private lateinit var busDiscovery: BusRouteAndStopDiscovery

    /// Need to save the routes and bus stops for the particular bus from the input stream.
    /// These 3 values cant be NULL because user need to update these things in order to register and use the app.
    private lateinit var busStops: ListState<BusStop>
    private lateinit var busRoutes: ListState<Route>
    private lateinit var busRouteType: ValueState<RouteType> // also output stream.

    /// Need to find out these values for output stream.
    private lateinit var recentCoordinates: ListState<TimeStampedCoordinate>
    private lateinit var lastPassedBusStop: ValueState<Pair<TimeStamp?,BusStop>>
    private lateinit var nextBusStop: ValueState<BusStop>
    private lateinit var currentStop: ValueState<BusStop>
    private lateinit var isReturning: ValueState<Boolean>


    override fun open(openContext: OpenContext?) {

        busDiscovery = BusRouteAndStopDiscovery()

        runtimeContext.apply {
            busStops = getListState<BusStop>(BUS_STOPS)
            busRoutes = getListState<Route>(BUS_ROUTES)
            busRouteType = getValueState<RouteType>(BUS_ROUTE_TYPE)
            recentCoordinates = getListState<TimeStampedCoordinate>(RECENT_COORDINATES)
            lastPassedBusStop = getValueState<Pair<TimeStamp?,BusStop>>(LAST_PASSED_BUS_STOP)
            nextBusStop = getValueState<BusStop>(NEXT_BUS_STOP)
            currentStop = getValueState<BusStop>(CURRENT_STOP)
            isReturning = getValueState<Boolean>(IS_RETURNING)
        }
    }

    override fun processElement(
        element: EitherOfThree<BusLocationData, BusData, Route>,
        context: KeyedProcessFunction<String, EitherOfThree<BusLocationData, BusData, Route>, BusLocationWithMetadata>.Context,
        out: Collector<BusLocationWithMetadata>
    ) {
        when (element) {

            // Processing,updating and collecting the Bus LOCATION and METADATA.
            is EitherOfThree.Left -> {

                /// Discovering Bus MetaData if not available already.
                if (lastPassedBusStop.value() == null || nextBusStop.value() == null || isReturning.value() == null) {
                    findNextLastStopAndIfIsReturningFromScratch(element.value)
                }

                /// Checking If Bus reached the destination stop and adjusting the metadata accordingly.
                checkIfBusReachedDestAndUpdateMetadata(element.value)


                /// Getting a POINT from available routes between set of stops.
                if (lastPassedBusStop.value() != null || nextBusStop.value() != null || isReturning.value() != null)
                getPointInRouteFromAvailableRoutesAndCollectAndUpdateMetadata(
                    busLocation = element.value,
                ) { point,currentRoute ->
                    out.collect(
                        BusLocationWithMetadata(
                            busId = context.currentKey,
                            currentRoute = currentRoute,
                            routeType = busRouteType.value(),
                            location = TimeStampedCoordinate(
                                timestamp = element.value.timestamp,
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

            is EitherOfThree.Middle -> {
                element.value.let { busData ->
                    busStops.update(busData.stops)
                    busRouteType.update(busData.routeType)
                }
            }

            is EitherOfThree.Right -> {
                busRoutes.add(element.value)
            }
        }
    }


    private fun getPointInRouteFromAvailableRoutesAndCollectAndUpdateMetadata(
        busLocation: BusLocationData,
        collect: (point: Coordinate,currRoute: Route) -> Unit
    ){
        busDiscovery.getPointInRoute(
            currentPoint = busLocation.coordinate,
            lastPassedStopId = lastPassedBusStop.value().second.stopId,
            nextStopId = nextBusStop.value().stopId,
            busRoutes = busRoutes.get().toList(),
        ).let { pointInRoute ->
            pointInRoute?.let {
                val currRoute = busRoutes.get().toList().find { route -> it.routeId == route.routeId}!!
                collect(it.coordinate,currRoute)
            }

            /* In case bus was not running on the available routes, maybe it is running on some other route
             between the same set of stops, but we don't have that route saved yet. Later this can be solved by calling
             MapBox directions Api but for NOW let's assume its on another route between different set of stops.*/
            pointInRoute ?: busDiscovery.getPointInRouteFromScratch(
                busLocation.coordinate, busRoutes.get().toList()
            ).let { pointInRoute ->
                val isRunningBetweenDiffPoints = pointInRoute != null
                /* In case bus is running on different set of stops, we are clearing all the stale metadata so
                 it had to calculate fresh again. */
                if (isRunningBetweenDiffPoints) cleaMetaData()
                /* TODO("What if bus is not running between any set of Stops ?") */
            }
        }
    }

    private fun checkIfBusReachedDestAndUpdateMetadata(
        busLocation: BusLocationData
    ){
        val stops = busStops.get().toList()
        stops.forEach { stop ->
            val hasReachedStop =  busDiscovery.checkIfCurrentPointIsWithinBusStopRadius(
                currentPoint = busLocation.coordinate,
                stop.coordinates
            )
            /// In case bus reached the intended BusStop
            if (hasReachedStop) {
                //// We are making sure it won't update the same Current Stop again and again in case on the same stop for long.
                val currStop = currentStop.value()
                if ( currStop == null || currStop.stopId != stop.stopId ) {
                    // Only update the current stop if that's the stop bus was intended to reach.
                    if (stop.stopId == nextBusStop.value().stopId){
                        currentStop.update(stop)
                        lastPassedBusStop.update(busLocation.timestamp to stop)
                        busDiscovery.getNextStopIndex(
                            currentStopIndex = stops.indexOf(stop),
                            totalBusStopsIndex = stops.size - 1,
                            routeType = busRouteType.value(),
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
            busStops.get().toList()
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
                    routeType = busRouteType.value(),
                    routes = busRoutes.get().toList(),
                    busStops = busStops.get().toList(),
                    recentCoordinates = recentCoordinates.get().map { it.toCoordinate() }
                )
                busDiscoveryResult?.let {
                    val lastPassedStop = busStops.get().toList()[it.lastPassedStopIndex]
                    val nextStop = busStops.get().toList()[it.nextStopIndex]
                    lastPassedBusStop.update(null to lastPassedStop)
                    nextBusStop.update(nextStop)
                    isReturning.update(it.isReturning)
                    recentCoordinates.clear()  //// Clear the list after getting the result.
                }
            }
        }
    }

    private fun cleaMetaData(){
        nextBusStop.clear()
        lastPassedBusStop.clear()
        isReturning.clear()
    }


    ///override fun close() {super.close()}

    companion object {
        private const val RECENT_COORDINATES = "RECENT_COORDINATES"
        private const val BUS_STOPS = "BUS_STOPS"
        private const val BUS_ROUTES = "BUS_ROUTES"
        private const val BUS_ROUTE_TYPE = "BUS_ROUTE_TYPE"
        private const val LAST_PASSED_BUS_STOP = "LAST_PASSED_BUS_STOP"
        private const val NEXT_BUS_STOP = "NEXT_BUS_STOP"
        private const val CURRENT_STOP = "CURRENT_STOP"
        private const val IS_RETURNING = "IS_RETURNING"
    }
}
