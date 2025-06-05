package app

import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import models.Coordinates
import models.Route
import java.time.Duration
import java.time.Instant


//// window is 10 sec long.
//// see if you can do anything with the window starting and ending time. // may not need it.
//// in function maybe ignore the noises like small movement.
////  only call ETA fun when you know Which route bus is on , its next stop direction  and its last stop arrival time (if possible to get most accurate out of this fun.).


//class BusTrackingUtility {
//
//    /// Failing to provide CurrentStopDetails Arrival time would affect the accuracy of this function.
//    fun calculateEta(
//        waitTime: Duration,
//        oldestPoint: Coordinates,
//        latestCoordinate: Coordinates,
//        currentRoute: Route,
//        currentStopDetails: CurrentStopDetails,
//        isReturning: Boolean, // will always true if it's a LOOP route
//    ){
//
//    }
//
//}

data class CurrentStopDetails(
    val latitude: String,
    val longitude: String,
    val arrivalTime: Duration?,
)

sealed interface EtaResult {
    data class Ahead(val eta: Duration, val delta: Duration) : EtaResult
    data class Delayed(val eta: Duration, val delta: Duration) : EtaResult
}


/// Maybe need to

object EtaCalculator {

    fun calculateEta(
        waitTime: Duration,
        oldestPoint: Coordinates,
        latestCoordinate: Coordinates,
        currentRoute: Route,
        currentStopDetails: CurrentStopDetails,
        isReturning: Boolean
    ): EtaResult? {
        val startPoint = getStartPoint(oldestPoint, currentStopDetails)
        val startTimeMillis = getStartTimeMillis(oldestPoint, currentStopDetails) ?: return null
        val now = System.currentTimeMillis()

        if (!hasWaitedEnough(waitTime, startTimeMillis, now)) return null  /// NOW ? i think this need to be changed

        val orderedRoutePoints = orderRoutePoints(currentRoute.coordinates, isReturning)
        val routePoints = convertToPoints(orderedRoutePoints)

        val currentPos = getCurrentPosition(latestCoordinate) ?: return null
        val snappedIndex = getNearestPointIndex(currentPos, routePoints)
        val remainingPoints = routePoints.subList(snappedIndex, routePoints.size)
        if (remainingPoints.size < 2) return null

        val remainingDistance = getLineLength(remainingPoints)
        val expectedSpeed = getExpectedSpeed(currentRoute)

        val timeElapsedSeconds = getTimeElapsedSeconds(startTimeMillis, now)
        if (timeElapsedSeconds <= 0) return null

        val actualDistance = TurfMeasurement.distance(startPoint, currentPos, "meters")
        val expectedDistance = expectedSpeed * timeElapsedSeconds
        val deltaDistance = actualDistance - expectedDistance

        return calculateEtaResult(
            deltaDistance = deltaDistance,
            remainingDistance = remainingDistance,
            expectedSpeed = expectedSpeed
        )
    }

    private fun getStartPoint(oldestPoint: Coordinates, currentStopDetails: CurrentStopDetails): Point {
        return if (currentStopDetails.arrivalTime != null) {
            Point.fromLngLat(currentStopDetails.longitude.toDouble(), currentStopDetails.latitude.toDouble())
        } else {
            Point.fromLngLat(
                oldestPoint.longitude.toDouble(),
                oldestPoint.latitude.toDouble()
            )
        }
    }

    private fun getStartTimeMillis(oldestPoint: Coordinates, currentStopDetails: CurrentStopDetails): Long? {
        return currentStopDetails.arrivalTime?.toMillis()
            ?: Instant.parse(oldestPoint.timeStamp).toEpochMilli()
    }

    private fun hasWaitedEnough(waitTime: Duration, startTimeMillis: Long, now: Long): Boolean {
        val waitedSoFar = Duration.ofMillis(now - startTimeMillis)
        return waitedSoFar >= waitTime
    }

    private fun orderRoutePoints(coordinates: List<Coordinates>, isReturning: Boolean): List<Coordinates> {
        return if (isReturning) coordinates.asReversed() else coordinates
    }

    private fun convertToPoints(coordinates: List<Coordinates>): List<Point> {
        return coordinates.map { Point.fromLngLat(it.longitude.toDouble(), it.latitude.toDouble()) }
    }

    private fun getCurrentPosition(latestCoordinate: Coordinates): Point? {
        return Point.fromLngLat(latestCoordinate.longitude.toDouble(), latestCoordinate.latitude.toDouble())
    }

    private fun getNearestPointIndex(currentPos: Point, routePoints: List<Point>): Int {
        val lineString = LineString.fromLngLats(routePoints)
        val snapped = TurfMeasurement.nearestPointOnLine(currentPos, lineString.coordinates())
        return snapped.properties()?.get("index")?.asInt ?: 0
    }

    private fun getLineLength(points: List<Point>): Double {
        val line = LineString.fromLngLats(points)
        return TurfMeasurement.length(line, "meters")
    }

    private fun getExpectedSpeed(route: Route): Double {
        val totalDistance = route.distanceInMeters.toDouble()
        val totalDuration = route.duration.seconds.toDouble()
        return totalDistance / totalDuration
    }

    private fun getTimeElapsedSeconds(startTimeMillis: Long, now: Long): Double {
        return Duration.ofMillis(now - startTimeMillis).seconds.toDouble()
    }

    private fun calculateEtaResult(
        deltaDistance: Double,
        remainingDistance: Double,
        expectedSpeed: Double
    ): EtaResult {
        val timeCorrectionSeconds = deltaDistance / expectedSpeed
        val etaSeconds = remainingDistance / expectedSpeed
        val adjustedEtaSeconds = etaSeconds - timeCorrectionSeconds

        val adjustedEta = Duration.ofSeconds(adjustedEtaSeconds.toLong())
        val delta = Duration.ofSeconds(kotlin.math.abs(timeCorrectionSeconds).toLong())

        return if (deltaDistance >= 0) {
            EtaResult.Ahead(adjustedEta, delta)
        } else {
            EtaResult.Delayed(adjustedEta, delta)
        }
    }
}


/**
Got it! Here’s a **more accurate, step-by-step algorithm with an example** based on your code:

---

### Algorithm for ETA Calculation

**Inputs:**

* `waitTime`: Minimum time to wait before calculating ETA (e.g., 30 seconds)
* `oldestPoint`: Previous known location and timestamp of the vehicle
* `latestCoordinate`: Current vehicle location and timestamp
* `currentRoute`: Route details (coordinates, total distance, total duration)
* `currentStopDetails`: Details of the current stop (arrival time and location)
* `isReturning`: Boolean indicating if the vehicle is on a return trip (reverses route order)

---

### Steps:

1. **Determine Start Point & Time**

* If `currentStopDetails.arrivalTime` exists, use its location and timestamp as start.
* Else, use `oldestPoint`’s location and timestamp.

*Example*:
If arrivalTime is `2025-06-05T10:00:00Z` at stop lat=12.97, lon=77.59, use this as start.
Else fallback to oldestPoint timestamp `2025-06-05T09:55:00Z` at lat=12.95, lon=77.58.

2. **Check Waiting Time**

* Calculate elapsed time since start.
* If elapsed time < `waitTime` (e.g., 30s), do not calculate ETA yet (return null).

3. **Order Route Points**

* If `isReturning` is true, reverse route coordinates.
* Otherwise, keep as is.

4. **Find Vehicle's Current Position on Route**

* Convert route coordinates to points.
* Snap `latestCoordinate` (current position) to nearest point on the route line.
* Find index of this snapped point.

5. **Calculate Remaining Distance**

* Get the list of points from snapped index to end of route.
* Calculate total distance of remaining route.

6. **Calculate Expected Speed**

* `expectedSpeed = totalRouteDistance / totalRouteDuration` (meters per second)

7. **Calculate Time Elapsed**

* Elapsed time (seconds) = `currentTime - startTime`

8. **Calculate Actual Distance Traveled**

* Distance between start point and current position.

9. **Calculate Expected Distance Traveled**

* `expectedDistance = expectedSpeed * elapsedTime`

10. **Calculate Delta**

* `deltaDistance = actualDistance - expectedDistance`

11. **Calculate Adjusted ETA**

* Time correction = `deltaDistance / expectedSpeed`
* Base ETA = `remainingDistance / expectedSpeed`
* Adjusted ETA = `base ETA - time correction`

12. **Return Result**

* If `deltaDistance >= 0`, vehicle is ahead of schedule → return `EtaResult.Ahead`
* Else delayed → return `EtaResult.Delayed`

---

### Example Walkthrough:

* `waitTime = 30s`
* Start at stop arrival time `10:00:00` at lat=12.97, lon=77.59
* Current time `10:02:00` → elapsed 120 seconds > waitTime → proceed
* Route distance = 10,000 meters, duration = 1200 seconds → expected speed = 8.33 m/s
* Remaining distance after snapping current position = 4,000 meters
* Actual distance traveled from start = 1,100 meters
* Expected distance traveled in 120 seconds = 8.33 \* 120 = 1000 meters
* Delta = 1100 - 1000 = +100 meters → vehicle is ahead by 100 meters
* Time correction = 100 / 8.33 ≈ 12 seconds
* Base ETA = 4000 / 8.33 ≈ 480 seconds
* Adjusted ETA = 480 - 12 = 468 seconds (\~7 min 48 sec)
* Return `EtaResult.Ahead(Duration.ofSeconds(468), delta)`

---

Let me know if you want me to format it differently or add more detail!
**/
