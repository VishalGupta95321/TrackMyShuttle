package app

import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc
import models.BusStop
import models.Coordinate
import models.Route
import models.toPoint
import util.RouteType
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val MAX_TIME_INTERVAL_BETWEEN_COORDINATES_IN_LIST_IN_SEC = 20L
private const val MAX_TOTAL_DISTANCE_OF_COORDINATES_IN_LIST_IN_METERS = 150L
private const val BUS_STOP_RADIUS_IN_METERS = 100L
private const val MAX_GPS_ERROR_IN_METERS = 20L

private typealias Index = Int
private typealias TimeStamp = Long

data class NearestPoint(
    val coordinate: Coordinate,
    val distanceInMeters: Double,
)

data class PointInRoute(
    val routeId: String,
    val coordinate: Coordinate
)


class BusPathDiscovery {


    private fun getNextStop(
        currentStopIndex: Int,
        totalBusStopsIndex: Int,
        routeType: RouteType,
    ){
        when(routeType){
            RouteType.Loop -> {

            }
            RouteType.OutAndBack -> {

            }
        }
    }

    private fun findNextLastStopAndIfIsReturningFromScratch(
        routeType: RouteType,
        currRouteStop1: Pair<Index, BusStop>,
        currRouteStop2: Pair<Index, BusStop>,
        totalBusStopsIndex: Int,
        recentCoordinates: List<Coordinate>,
        result: (lastPassedStopIndex: Int, nextStopIndex: Int, isReturning: Boolean) -> Unit
    ) {
        var nextStopIndex: Int? = null
        var lastPassedStopIndex: Int? = null
        var isReturning: Boolean? = false


        /// In case any point in the recent coordinates list reached a stop.
        recentCoordinates.forEach { coord ->
            val distanceFromStop1 =
                TurfMeasurement.distance(coord.toPoint(), currRouteStop1.second.coordinates.toPoint(),TurfConstants.UNIT_METERS)
            val distanceFromStop2 =
                TurfMeasurement.distance(coord.toPoint(), currRouteStop2.second.coordinates.toPoint(),TurfConstants.UNIT_METERS)
            if (distanceFromStop1 <= BUS_STOP_RADIUS_IN_METERS){

            }
        }

        /// In case bus is still heading towards the stop
        val firstPoint = recentCoordinates.first()
        val lastPoint = recentCoordinates.last()

        val stop1DistanceFromFirstPoint = TurfMeasurement.distance(
            firstPoint.toPoint(),
            currRouteStop1.second.coordinates.toPoint(),
            TurfConstants.UNIT_METERS
        )
        val stop2DistanceFromFirstPoint = TurfMeasurement.distance(
            firstPoint.toPoint(),
            currRouteStop2.second.coordinates.toPoint(),
            TurfConstants.UNIT_METERS
        )

        val stop1DistanceFromLastPoint =
            TurfMeasurement.distance(lastPoint.toPoint(), currRouteStop1.second.coordinates.toPoint(), TurfConstants.UNIT_METERS)
        val stop2DistanceFromLastPoint =
            TurfMeasurement.distance(lastPoint.toPoint(), currRouteStop2.second.coordinates.toPoint(), TurfConstants.UNIT_METERS)

        when (routeType) {

            RouteType.Loop -> {
                if (currRouteStop1.first == totalBusStopsIndex && currRouteStop2.first == 0 ||
                    currRouteStop2.first == totalBusStopsIndex && currRouteStop1.first == 0
                ) {
                    nextStopIndex = 0
                    lastPassedStopIndex = totalBusStopsIndex
                }

                if (currRouteStop1.first < currRouteStop2.first) {
                    lastPassedStopIndex = currRouteStop1.first
                    nextStopIndex = currRouteStop2.first
                } else {
                    lastPassedStopIndex = currRouteStop2.first
                    nextStopIndex = currRouteStop1.first
                }

                /// It's going to be always false because the bus is running in the Loop in the same direction.
                isReturning = false
            }

            RouteType.OutAndBack -> {
                if (stop1DistanceFromLastPoint < stop1DistanceFromFirstPoint) {
                    nextStopIndex = currRouteStop1.first
                    lastPassedStopIndex = currRouteStop2.first
                    isReturning = nextStopIndex < lastPassedStopIndex
                }
                if (stop2DistanceFromLastPoint < stop2DistanceFromFirstPoint) {
                    nextStopIndex = currRouteStop2.first
                    lastPassedStopIndex = currRouteStop1.first
                    isReturning = nextStopIndex < lastPassedStopIndex
                }
            }
        }
        result(lastPassedStopIndex!!,nextStopIndex!!,isReturning!!)
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


    ///// Clearing the recent is the time interval between the next coordinate exceeds the interval time.
    ///// Got Called everytime we add new point in the recentCoord list.
    fun clearRecentCoordinatesIfIntervalPassed(
        recentCoordinates: List<Pair<Long, Coordinate>>,
        currentCoordinateTimestamp: Long,
        result: (needToClear: Boolean) -> Unit
    ) {
        if (recentCoordinates.isNotEmpty()) {
            val lastCoordinate = recentCoordinates.last()
            val timeDiffInSecs = (currentCoordinateTimestamp - lastCoordinate.first).toDuration(DurationUnit.SECONDS)
            if (timeDiffInSecs.inWholeSeconds >= MAX_TIME_INTERVAL_BETWEEN_COORDINATES_IN_LIST_IN_SEC) {
                result(true)
            }
        }
        result(false)
    }

    //// Checking if we have enough recent coordinates
    //// We will call findNextLastStopAndIfIsReturningFromScratch() after this function
    fun checkIfCoordinatesListCoveredTotalDistance(
        recentCoordinates: List<Pair<TimeStamp, Coordinate>>,
        result: (isDistanceCovered: Boolean) -> Unit
    ) {
        if (recentCoordinates.size < 2) result(false)

        val points = recentCoordinates.map {
            Point.fromLngLat(it.second.longitude.toDouble(), it.second.latitude.toDouble())
        }
        val lineString = LineString.fromLngLats(points)
        val distanceInMeters = TurfMeasurement.length(lineString, "meters")

        result(distanceInMeters >= MAX_TOTAL_DISTANCE_OF_COORDINATES_IN_LIST_IN_METERS)
    }


}