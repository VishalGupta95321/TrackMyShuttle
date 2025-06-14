package app

import app.BusRouteAndStopDiscovery.Companion.BUS_STOP_RADIUS_IN_METERS
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import models.Coordinate
import models.EtaResult
import models.TimeStampedCoordinate
import util.RouteType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


////  only call ETA fun when you know Which route bus is on , its next stop direction  and its last stop arrival time (if possible to get most accurate out of this fun.).
/// There are 2 functions NearestOnLine and Nearest

data class FromStopDetails(
    val latitude: String,
    val longitude: String,
    val waitTime: Duration,
    val arrivalTime: Duration? // kotlin.time.Duration
)

data class CurrentRouteDetails(
    val busId: String,
    val routeId: String,
    val coordinates: List<Coordinate>,
    val duration: Duration,
    val distanceInMeters: String,
    val routeType: RouteType,
    val isReturning: Boolean,
)



// Calculates ETA of the bus and also returns the remaining distance.
class EtaCalculator {

    // Failing to provide CurrentStopDetails Arrival time would affect the accuracy of this function.
    @OptIn(ExperimentalTime::class)
    fun calculateEta(
        oldestCoordinates: TimeStampedCoordinate,
        latestCoordinate: TimeStampedCoordinate,
        currentRoute: CurrentRouteDetails,
        fromStop: FromStopDetails, // from the stop where bus started its journey
    ): EtaResult? {

        val isReturning = when(currentRoute.routeType){
            RouteType.Loop -> false
            is RouteType.OutAndBack -> currentRoute.isReturning
        }

        val startPoint = getStartPoint(oldestCoordinates, fromStop)

        val startTimeMillis = getStartTimeMillis(oldestCoordinates, fromStop)

        val nowMillis = latestCoordinate.timestamp.milliseconds.inWholeMilliseconds


        if (fromStop.arrivalTime != null) {
            if (!hasWaitedEnough(fromStop.waitTime, startTimeMillis, nowMillis)) return null  ///////
        }

        val orderedRoutePoints = orderRoutePoints(currentRoute.coordinates, isReturning)
        val routePoints = convertToPoints(orderedRoutePoints)

        val currentPos = getCurrentPosition(latestCoordinate)
        val nearestPointInLineIndex = getNearestPointIndex(currentPos, routePoints) ?: return null
        val remainingPoints = routePoints.subList(nearestPointInLineIndex, routePoints.size)
       //  if (remainingPoints.size < 2) return null

        val remainingDistance = getLineLength(remainingPoints) - BUS_STOP_RADIUS_IN_METERS ///// version 2
        val expectedSpeed = getExpectedSpeed(currentRoute)
        //if (expectedSpeed <= 0) return null

        val timeElapsedSeconds = getTimeElapsedSeconds(startTimeMillis, nowMillis)
       // if (timeElapsedSeconds <= 0) return null

        val actualDistance = TurfMeasurement.distance(startPoint, currentPos, "meters")
        val expectedDistance = expectedSpeed * timeElapsedSeconds
        val deltaDistance = actualDistance - expectedDistance

        return calculateEtaResult(
            deltaDistance = deltaDistance,
            remainingDistance = remainingDistance,
            expectedSpeed = expectedSpeed
        )
    }

    private fun getStartPoint(oldestPoint: TimeStampedCoordinate, fromStopDetails: FromStopDetails): Point {
        return if (fromStopDetails.arrivalTime != null) {
            Point.fromLngLat(
                fromStopDetails.longitude.toDouble(),
                fromStopDetails.latitude.toDouble()
            )
        } else {
            Point.fromLngLat(
                oldestPoint.coordinate.longitude.toDouble(),
                oldestPoint.coordinate.latitude.toDouble()
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getStartTimeMillis(oldestPoint: TimeStampedCoordinate, fromStopDetails: FromStopDetails): Long {
        return fromStopDetails.arrivalTime?.inWholeMilliseconds
            ?: oldestPoint.timestamp.milliseconds.inWholeMilliseconds
    }

    private fun hasWaitedEnough(waitTime: Duration, startTimeMillis: Long, nowMillis: Long): Boolean {
        val waitedSoFar = (nowMillis - startTimeMillis).milliseconds
        return waitedSoFar >= waitTime
    }

    private fun orderRoutePoints(coordinates: List<Coordinate>, isReturning: Boolean): List<Coordinate> {
        return if (isReturning) coordinates.asReversed() else coordinates
    }

    private fun convertToPoints(coordinates: List<Coordinate>): List<Point> {
        return coordinates.map { Point.fromLngLat(it.longitude.toDouble(), it.latitude.toDouble()) }
    }

    private fun getCurrentPosition(latestCoordinate: TimeStampedCoordinate): Point {
        return  Point.fromLngLat(latestCoordinate.coordinate.longitude.toDouble(), latestCoordinate.coordinate.latitude.toDouble())
    }

    private fun getNearestPointIndex(currentPos: Point, routePoints: List<Point>): Int? {
        var nearestIndex : Int? = null
        var minDistance = Double.MAX_VALUE
        for ((index, point) in routePoints.withIndex()) {
            val dist = TurfMeasurement.distance(currentPos, point, "meters")
            if (dist < minDistance) {
                minDistance = dist
                nearestIndex = index
            }
        }
        return nearestIndex
    }

    private fun getLineLength(points: List<Point>): Double {
        val line = LineString.fromLngLats(points)
        return TurfMeasurement.length(line, "meters")
    }

    private fun getExpectedSpeed(route: CurrentRouteDetails): Double {
        val totalDistance = route.distanceInMeters.toDoubleOrNull() ?: return 0.0
        val totalDurationSeconds = route.duration.inWholeSeconds.toDouble()
        return if (totalDurationSeconds > 0) totalDistance / totalDurationSeconds else 0.0
    }

    private fun getTimeElapsedSeconds(startTimeMillis: Long, nowMillis: Long): Double {
        val elapsedMillis = nowMillis - startTimeMillis
        return elapsedMillis.milliseconds.inWholeSeconds.toDouble()
    }

    private fun calculateEtaResult(
        deltaDistance: Double,
        remainingDistance: Double,
        expectedSpeed: Double
    ): EtaResult {
        val timeCorrectionSeconds = deltaDistance / expectedSpeed
        val etaSeconds = remainingDistance / expectedSpeed
        val adjustedEtaSeconds = etaSeconds - timeCorrectionSeconds

        val adjustedEta = adjustedEtaSeconds.coerceAtLeast(0.0).seconds
        val delta = kotlin.math.abs(timeCorrectionSeconds).seconds

        return if (deltaDistance >= 0) {
            EtaResult.Ahead(adjustedEta.inWholeSeconds, delta.inWholeSeconds, remainingDistance.toString())
        } else {
            EtaResult.Delayed(adjustedEta.inWholeSeconds, delta.inWholeSeconds, remainingDistance.toString())
        }
    }
}


/**
 * Algorithm for ETA (Estimated Time of Arrival) Calculation
 *
 * Inputs:
 * - waitTime: Minimum wait time before calculating ETA (e.g., 30 seconds)
 * - oldestPoint: Previous known location and timestamp of the vehicle for window.
 * - latestCoordinate: Current vehicle location and timestamp
 * - currentRoute: Route details (coordinates, total distance, total duration)
 * - fromStopDetails: from the stop where bus started its journey
 * - isReturning: If true, the vehicle is on a return trip (route is reversed)
 *
 * Steps:
 *
 * 1. Determine Start Point & Time
 *    - If fromStopDetails.arrivalTime exists, use it and the stop's location.
 *    - Otherwise, use oldestPoint's timestamp and location.
 *
 * 2. Check Waiting Time
 *    - Calculate elapsed time since start.
 *    - If elapsed time < waitTime, return null (not ready to calculate ETA).
 *
 * 3. Order Route Points
 *    - If isReturning is true, reverse the route coordinates.
 *
 * 4. Find Vehicle's Current Position on Route
 *    - Convert route coordinates to route points.
 *    - Snap latestCoordinate to the nearest point on the route.
 *    - Find index of this snapped point on the route.
 *
 * 5. Calculate Remaining Distance
 *    - Get the route segment from the current index to the end.
 *    - Sum the distance of remaining route points.
 *    - Minus the Bus Stop radius.
 *
 * 6. Calculate Expected Speed
 *    - expectedSpeed = totalRouteDistance / totalRouteDuration (in m/s)
 *
 * 7. Calculate Time Elapsed
 *    - elapsedTime = currentTime - startTime (in seconds)
 *
 * 8. Calculate Actual Distance Traveled
 *    - Measure distance from the start point to current snapped point.
 *
 * 9. Calculate Expected Distance Traveled
 *    - expectedDistance = expectedSpeed * elapsedTime
 *
 * 10. Calculate Delta
 *    - deltaDistance = actualDistance - expectedDistance
 *
 * 11. Calculate Adjusted ETA
 *    - timeCorrection = deltaDistance / expectedSpeed
 *    - baseETA = remainingDistance / expectedSpeed
 *    - adjustedETA = baseETA - timeCorrection
 *
 * 12. Return Result
 *    - If deltaDistance >= 0, vehicle is ahead of schedule:
 *         return EtaResult.Ahead(adjustedETA, delta)
 *    - Else, vehicle is delayed:
 *         return EtaResult.Delayed(adjustedETA, delta)
 *
 * Example:
 * - waitTime = 30s
 * - Start time = 10:00:00 at lat=12.97, lon=77.59
 * - Current time = 10:02:00 → elapsed = 120s
 * - Route distance = 10,000 meters; duration = 1,200s → speed = 8.33 m/s
 * - Actual distance traveled = 1,100 meters
 * - Expected distance = 8.33 * 120 = 1,000 meters
 * - Delta = +100 meters
 * - Correction = 100 / 8.33 ≈ 12s
 * - Base ETA = 4000 / 8.33 ≈ 480s
 * - Adjusted ETA = 480 - 12 = 468s
 * - Result = Ahead, ETA ≈ 7 min 48 sec
 */
