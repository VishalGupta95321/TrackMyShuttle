package app

import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc
import models.BusStop
import models.Coordinate
import models.Route
import models.TimeStampedCoordinate
import models.toPoint
import util.RouteType
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val MAX_TIME_INTERVAL_BETWEEN_COORDINATES_IN_LIST_IN_SEC = 20L
private const val MAX_TOTAL_DISTANCE_OF_COORDINATES_IN_LIST_IN_METERS = 100L
private const val BUS_STOP_RADIUS_IN_METERS = 100L
private const val MAX_GPS_ERROR_IN_METERS = 20L
private const val INDEX_ZERO = 0
private typealias Index = Int
private typealias TimeStamp = Long
private typealias isReturning = Boolean

data class NearestPoint(
    val coordinate: Coordinate,
    val distanceInMeters: Double,
)

data class PointInRoute(
    val routeId: String,
    val coordinate: Coordinate
)

data class BusDiscoveryResult(
    val lastPassedStopIndex: Int,
    val nextStopIndex: Int,
    val isReturning: Boolean
)

class BusRouteAndStopDiscovery {

    fun findNextLastStopAndIfIsReturningFromScratch(
        routeType: RouteType,
        routes: List<Route>,
        busStops: List<BusStop>,
        recentCoordinates: List<Coordinate>,
    ): BusDiscoveryResult? {
        var nextStopIndex: Int? = null
        var lastPassedStopIndex: Int? = null
        var isReturning: Boolean? = false

        val totalBusStopsIndex = busStops.size - 1

        /// We are taking the current route acc. to the first point in the recent coord list or when the bus first showed up.
        val currentRoute =
            getPointInRouteFromScratch(recentCoordinates.first(), routes)?.routeId.let { id ->
                routes.find { it.routeId == id }
            }

        currentRoute ?: return null

        val stop1 = currentRoute.fromStopId.let { stopId -> busStops.find { it.stopId == stopId } }
        val stop1Index = busStops.indexOf(stop1)

        val stop2 = currentRoute.toStopId.let { stopId -> busStops.find { it.stopId == stopId } }
        val stop2Index = busStops.indexOf(stop2)

        val currentStop1 = stop1Index to stop1!!
        val currentStop2 = stop2Index to stop2!!


        /// In case bus is still heading towards the stop
        val firstPoint = recentCoordinates.first()
        val lastPoint = recentCoordinates.last()

        val stop1DistanceFromFirstPoint = TurfMeasurement.distance(
            firstPoint.toPoint(),
            currentStop1.second.coordinates.toPoint(),
            TurfConstants.UNIT_METERS
        )
        val stop2DistanceFromFirstPoint = TurfMeasurement.distance(
            firstPoint.toPoint(),
            currentStop2.second.coordinates.toPoint(),
            TurfConstants.UNIT_METERS
        )

        val stop1DistanceFromLastPoint =
            TurfMeasurement.distance(
                lastPoint.toPoint(),
                currentStop1.second.coordinates.toPoint(),
                TurfConstants.UNIT_METERS
            )
        val stop2DistanceFromLastPoint =
            TurfMeasurement.distance(
                lastPoint.toPoint(),
                currentStop2.second.coordinates.toPoint(),
                TurfConstants.UNIT_METERS
            )

        if (stop1DistanceFromLastPoint < stop1DistanceFromFirstPoint) {
            println("Here 1")
            nextStopIndex = currentStop1.first
            lastPassedStopIndex = currentStop2.first
            isReturning = checkIfBusIsReturning(routeType, nextStopIndex, lastPassedStopIndex)
        }
        if (stop2DistanceFromLastPoint < stop2DistanceFromFirstPoint) {
            println("Here 2")
            nextStopIndex = currentStop2.first
            lastPassedStopIndex = currentStop1.first
            isReturning = checkIfBusIsReturning(routeType, nextStopIndex, lastPassedStopIndex)
        }

        /// In case any point in the recent coordinates list reached or crossed on of the stop.
        recentCoordinates.forEach { coordinates ->
            val isCurrentPointIsWithinStop1Radius =
                checkIfCurrentPointIsWithinBusStopRadius(coordinates, currentStop1.second.coordinates)

            val isCurrentPointIsWithinStop2Radius =
                checkIfCurrentPointIsWithinBusStopRadius(coordinates, currentStop2.second.coordinates)

            if (isCurrentPointIsWithinStop1Radius) {
                println(" Bus Crossed the Stop")
                lastPassedStopIndex = currentStop1.first
                nextStopIndex = getNextStopIndex(
                    currentStopIndex = currentStop1.first,
                    totalBusStopsIndex = totalBusStopsIndex,
                    routeType = routeType,
                    isReturning = checkIfBusIsReturning(
                        routeType = routeType,
                        nextOrReachedStopIndex = currentStop1.first,  /// Though this bus stop is reached but still if we are considering it did not reach. Just to find out if the Bus is returning is not.
                        lastStopIndex = currentStop2.first, /// If the bus reached or crossed Stop1 then its certain Stop2 was the last stop. /// There are just two possibilities for both of the stop either last stop and next stop.
                    )
                ).let {
                    isReturning = it.second
                    it.first
                }
            }

            if (isCurrentPointIsWithinStop2Radius) {
                println(" Bus Crossed the Stop")
                lastPassedStopIndex = currentStop2.first
                nextStopIndex = getNextStopIndex(
                    currentStopIndex = currentStop2.first,
                    totalBusStopsIndex = totalBusStopsIndex,
                    routeType = routeType,
                    isReturning = checkIfBusIsReturning(
                        routeType = routeType,
                        nextOrReachedStopIndex = currentStop2.first,
                        lastStopIndex = currentStop1.first,
                    )
                ).let {
                    isReturning = it.second
                    it.first
                }
            }
        }

        return BusDiscoveryResult(lastPassedStopIndex!!, nextStopIndex!!, isReturning!!)
    }

    // Only get called if already know the values required in the function's arguments
    fun getNextStopIndex(
        currentStopIndex: Int,
        totalBusStopsIndex: Int,
        routeType: RouteType,
        isReturning: Boolean
    ): Pair<Index, isReturning> {  /// returning isReturning in case its changed after changing the next stop.
        return when (routeType) {
            RouteType.Loop -> {
                if (currentStopIndex == totalBusStopsIndex) {
                    INDEX_ZERO to false
                }
                currentStopIndex + 1 to false
            }

            RouteType.OutAndBack -> {
                if (currentStopIndex == totalBusStopsIndex) {
                    currentStopIndex - 1 to true
                }
                if (isReturning) {
                    currentStopIndex - 1 to true //false
                } else currentStopIndex + 1 to false
            }
        }
    }

    private fun checkIfBusIsReturning(
        routeType: RouteType,
        nextOrReachedStopIndex: Int,
        lastStopIndex: Int,
    ): Boolean {
        return if (routeType is RouteType.OutAndBack) {
            println("Next or reached -- $nextOrReachedStopIndex")
            println("Last -- $lastStopIndex")

            nextOrReachedStopIndex < lastStopIndex
        } else false
    }


    /// Will call if we already know last,next stop and isReturning and wants to know where and in which Route the bus is running.
    /// TODO(" we can add functionality to fetch route from the api in case bus is not running on the available routes. ")
    fun getPointInRoute(
        currentPoint: Coordinate,
        lastPassedStopId: String,
        nextStopId: String,
        busRoutes: List<Route>
    ): PointInRoute? {
        val availableRoutes = busRoutes.filter {
            (it.fromStopId == nextStopId && it.toStopId == lastPassedStopId) ||
                    (it.fromStopId == lastPassedStopId && it.toStopId == nextStopId)
        }

        availableRoutes.sortedBy { it.routeCount }.forEach { route ->
            nearestPointOnLine(currentPoint, route).let { nearestPoint ->
                if (nearestPoint.distanceInMeters <= MAX_GPS_ERROR_IN_METERS)
                    return PointInRoute(
                        routeId = route.routeId,
                        nearestPoint.coordinate
                    )
            }
        }
        return null
    }

    /// same as getPointInRoute() but when you dont know other details if the bus like next, last stop, isReturning.
    // Only called when figuring last,next stop from scratch.
    fun getPointInRouteFromScratch(
        currentPoint: Coordinate,
        busRoutes: List<Route>
    ): PointInRoute? {
        busRoutes.sortedBy { it.routeCount }.forEach { route ->
            nearestPointOnLine(currentPoint, route).let { nearestPoint ->
                if (nearestPoint.distanceInMeters <= MAX_GPS_ERROR_IN_METERS)
                    return PointInRoute(
                        routeId = route.routeId,
                        nearestPoint.coordinate
                    )
            }
        }
        return null
    }

    private fun nearestPointOnLine(
        coordinate: Coordinate,
        route: Route,
    ): NearestPoint {
        val point = coordinate.toPoint()
        val nearestPoint =
            TurfMisc.nearestPointOnLine(point, route.coordinates.map { it.toPoint() }, TurfConstants.UNIT_METERS)
        val nearestCoord = Coordinate(
            (nearestPoint.geometry() as Point).coordinates().last().toString(),
            (nearestPoint.geometry() as Point).coordinates().first().toString()
        )
        val distanceToNearestCoord = nearestPoint.properties()!!.get("dist").toString()

        return NearestPoint(
            coordinate = nearestCoord,
            distanceInMeters = distanceToNearestCoord.toDouble()
        )
    }

    fun checkIfCurrentPointIsWithinBusStopRadius(
        currentPoint: Coordinate,
        busStopPoint: Coordinate,
    ): Boolean {
        val distanceFromStop =
            TurfMeasurement.distance(
                currentPoint.toPoint(),
                busStopPoint.toPoint(),
                TurfConstants.UNIT_METERS
            )
        return distanceFromStop <= BUS_STOP_RADIUS_IN_METERS
    }

    /// Clearing the recent coord list if the first point in the list is within Stop radius.
    fun clearRecentCoordinatesIfFirstPointIsWithinStopRadius(
        recentCoordinates: List<Coordinate>,
        busStops: List<BusStop>,
    ): Boolean {
        recentCoordinates.isNotEmpty().let {
            busStops.forEach { stop ->
                val result = checkIfCurrentPointIsWithinBusStopRadius(
                    currentPoint = recentCoordinates.first(),
                    busStopPoint = stop.coordinates
                )
                if (result) { return true }
            }
        }
        return false
    }


    ///// Clearing the recent is the time interval between the next coordinate exceeds the interval time.
    ///// Got Called everytime we add new point in the recentCoord list.
    fun clearRecentCoordinatesIfIntervalPassed(
        recentCoordinates: List<TimeStampedCoordinate>,
        currentCoordinateTimestamp: TimeStamp,
    ): Boolean {
        if (recentCoordinates.isNotEmpty()) {
            val lastCoordinate = recentCoordinates.last()
            val timeDiffInSecs = (currentCoordinateTimestamp - lastCoordinate.timestamp).toDuration(DurationUnit.SECONDS)
            if (timeDiffInSecs.inWholeSeconds >= MAX_TIME_INTERVAL_BETWEEN_COORDINATES_IN_LIST_IN_SEC) {
               return true
            }
        }
        return false
    }

    //// Checking if we have enough recent coordinates
    //// We will call findNextLastStopAndIfIsReturningFromScratch() after this function
    fun checkIfCoordinatesListCoveredTotalDistance(
        recentCoordinates: List<Coordinate>,
    ): Boolean {
        if (recentCoordinates.size < 2) return false

        val points = recentCoordinates.map {
            Point.fromLngLat(it.longitude.toDouble(), it.latitude.toDouble())
        }
        val lineString = LineString.fromLngLats(points)
        val distanceInMeters = TurfMeasurement.length(lineString, "meters")

        return distanceInMeters >= MAX_TOTAL_DISTANCE_OF_COORDINATES_IN_LIST_IN_METERS
    }
}