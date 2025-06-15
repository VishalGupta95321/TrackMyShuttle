import app.*
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import models.BusData
import models.BusLocationData
import models.BusStop
import models.Coordinate
import models.Route
import models.toPoint
import util.RouteType
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import java.util.Properties
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord


//// ** I don't think we need drivers to set route manually, just getting stops and route type would be enough.
///// ** Give driver an option to Mark Bus Active/InActive/InMaintenance, when they Activate the bus they have to enter Next and last next stop.
///// So we can update that data initially.
///// In BUS STOP you may also ask the stop radius.
////// While receiving no bus location data in the app on the tracking screen then SHOW loading screen like "Location the Bus.... Waiting for bus to come online."
// something like that ALSO in between also check if bus went INACTIVE is that the case tell the user bus is Inactive and return to the home screen.
///// SEND with key-value pair from sink that would be to consume by consumer as they dont have to deserialize each record to check if its theirs.
/////// IF you get null in eta in app So stick with the previous ETA until you get new value

///////// in the app , The ETA value with 0s will arrive 10sec later then , the tracking data because of window , it means it will show reached current stop but it still  is 10 sec and not 0


import kotlin.math.*
import java.util.concurrent.TimeUnit


//fun generateRecentCoordinatesAlongRoute(
//    route: List<Coordinate>,
//    startTimestampMillis: Long = System.currentTimeMillis(),
//    maxTimeGapSeconds: Long = 15,
//    minTotalDistanceMeters: Double = 100.0
//): List<Pair<Long, Coordinate>> {
//    val result = mutableListOf<Pair<Long, Coordinate>>()
//
//    var totalDistance = 0.0
//    var currentTime = startTimestampMillis
//
//    for (i in 0 until route.lastIndex) {
//        val from = route[i]
//        val to = route[i + 1]
//
//        val segmentDistance = haversineDistanceMeters(
//            from.latitude.toDouble(), from.longitude.toDouble(),
//            to.latitude.toDouble(), to.longitude.toDouble()
//        )
//
//        if (segmentDistance <= 0.0) continue
//
//        // Subdivide segment into ~10m steps
//        val steps = max(1, (segmentDistance / 10).toInt())
//
//        for (j in 1..steps) {
//            val ratio = j.toDouble() / (steps + 1)
//            val lat = from.latitude.toDouble() + (to.latitude.toDouble() - from.latitude.toDouble()) * ratio
//            val lon = from.longitude.toDouble() + (to.longitude.toDouble() - from.longitude.toDouble()) * ratio
//
//            val snappedCoord = Coordinate(lat.toString(), lon.toString())
//            result.add(currentTime to snappedCoord)
//
//            // Update time
//            currentTime += TimeUnit.SECONDS.toMillis((5..maxTimeGapSeconds.toInt()).random().toLong())
//
//            // Update distance
//            totalDistance += segmentDistance / (steps + 1)
//
//            if (totalDistance >= minTotalDistanceMeters) {
//                return result
//            }
//        }
//    }
//
//    return result // May be less if segment too short
//}


//fun generateRecentCoordinatesFromSegment(
//    segment: List<Coordinate>,
//    busStops: List<BusStop>,
//    startTimestampMillis: Long = System.currentTimeMillis(),
//    maxTimeGapSeconds: Long = 15,
//    minTotalDistanceMeters: Double = 100.0,
//    busStopRadiusMeters: Double = 100.0
//): List<Pair<Long, Coordinate>> {
//    val output = mutableListOf<Pair<Long, Coordinate>>()
//    var currentTime = startTimestampMillis
//    var totalDistance = 0.0
//
//    for (i in 0 until segment.lastIndex) {
//        val start = segment[i]
//        val end = segment[i + 1]
//
//        val startLat = start.latitude.toDouble()
//        val startLon = start.longitude.toDouble()
//        val endLat = end.latitude.toDouble()
//        val endLon = end.longitude.toDouble()
//
//        val segmentDistance = haversineDistanceMeters(startLat, startLon, endLat, endLon)
//        if (segmentDistance <= 0.0) continue
//
//        val interpolations = max(1, (segmentDistance / 10).toInt()) // finer steps every ~10 meters
//        for (j in 1..interpolations) {
//            val fraction = j.toDouble() / (interpolations + 1)
//            val lat = startLat + (endLat - startLat) * fraction
//            val lon = startLon + (endLon - startLon) * fraction
//            val coord = Coordinate(lat.toString(), lon.toString())
//
//            // Check if this point is far enough from all bus stops
//            val isFarFromAllStops = busStops.all { stop ->
//                val stopLat = stop.coordinates.latitude.toDouble()
//                val stopLon = stop.coordinates.longitude.toDouble()
//                val dist = haversineDistanceMeters(lat, lon, stopLat, stopLon)
//                dist >= busStopRadiusMeters
//            }
//
//            if (isFarFromAllStops) {
//                output.add(currentTime to coord)
//                currentTime += TimeUnit.SECONDS.toMillis((5..maxTimeGapSeconds.toInt()).random().toLong())
//                totalDistance += segmentDistance / (interpolations + 1)
//
//                if (totalDistance >= minTotalDistanceMeters) {
//                    return output
//                }
//            }
//        }
//    }
//
//    return output // May be less if not enough valid distance
//}
//
// // Haversine formula
//fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
//    val R = 6371000.0 // Earth radius in meters
//    val dLat = Math.toRadians(lat2 - lat1)
//    val dLon = Math.toRadians(lon2 - lon1)
//    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
//    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//    return R * c
//}


//// Testing findNextLastStopAndIfIsReturningFromScratch() ///// SUCCESS

fun generateRecentCoordinatesFromSegment(
    segment: List<Coordinate>,
    busStops: List<BusStop>,
    startTimestampMillis: Long = System.currentTimeMillis(),
    maxTimeGapSeconds: Long = 15,
    minTotalDistanceMeters: Double = 100.0,
    busStopRadiusMeters: Double = 100.0
): List<Pair<Long, Coordinate>> {
    val output = mutableListOf<Pair<Long, Coordinate>>()
    var currentTime = startTimestampMillis
    var totalDistance = 0.0
    var firstPointAdded = false

    for (i in 0 until segment.lastIndex) {
        val start = segment[i]
        val end = segment[i + 1]

        val startLat = start.latitude.toDouble()
        val startLon = start.longitude.toDouble()
        val endLat = end.latitude.toDouble()
        val endLon = end.longitude.toDouble()

        val segmentDistance = haversineDistanceMeters(startLat, startLon, endLat, endLon)
        if (segmentDistance <= 0.0) continue

        val interpolations = max(1, (segmentDistance / 10).toInt()) // ~10 meters apart
        for (j in 1..interpolations) {
            val fraction = j.toDouble() / (interpolations + 1)
            val lat = startLat + (endLat - startLat) * fraction
            val lon = startLon + (endLon - startLon) * fraction
            val coord = Coordinate(lat.toString(), lon.toString())

            // Only check bus stop proximity for the FIRST point
            if (!firstPointAdded) {
                val isFarFromAllStops = busStops.all { stop ->
                    val stopLat = stop.coordinates.latitude.toDouble()
                    val stopLon = stop.coordinates.longitude.toDouble()
                    val dist = haversineDistanceMeters(lat, lon, stopLat, stopLon)
                    dist >= busStopRadiusMeters
                }

                if (!isFarFromAllStops) continue // skip and keep looking
            }

            output.add(currentTime to coord)
            currentTime += TimeUnit.SECONDS.toMillis((5..maxTimeGapSeconds.toInt()).random().toLong())
            totalDistance += segmentDistance / (interpolations + 1)

            if (!firstPointAdded) firstPointAdded = true

            if (totalDistance >= minTotalDistanceMeters) return output
        }
    }

    return output
}

// Haversine formula
fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}


fun formatCoordinatesForExport(coords: List<Coordinate>, color: String = "#0000FF"): List<String> {
    return coords.mapIndexed { index, coord ->
        "${coord.latitude},${coord.longitude},P${index + 1},$color"
    }
}




//// Testing findNextLastStopAndIfIsReturningFromScratch() ///// SUCCESS
@OptIn(ExperimentalTime::class)
fun mainn(){
    val routeS0S1 = listOf(
        Coordinate("40.755997", "-73.940509"),
        Coordinate("40.75629", "-73.940229"),
        Coordinate("40.756307", "-73.940212"),
        Coordinate("40.756487", "-73.94004"),
        Coordinate("40.756835", "-73.93972"),
        Coordinate("40.756886", "-73.939673"),
        Coordinate("40.75693", "-73.939633"),
        Coordinate("40.756985", "-73.939582"),
        Coordinate("40.757432", "-73.939163"),
        Coordinate("40.757503", "-73.939097"),
        Coordinate("40.757573", "-73.939028"),
        Coordinate("40.758006", "-73.9386"),
        Coordinate("40.758404", "-73.938217"),
        Coordinate("40.758615", "-73.938014"),
        Coordinate("40.758652", "-73.937978"),
        Coordinate("40.758722", "-73.937911"),
        Coordinate("40.758785", "-73.93785"),
        Coordinate("40.759258", "-73.937391"),
        Coordinate("40.759403", "-73.93725"),
        Coordinate("40.759605", "-73.937056"),
        Coordinate("40.759817", "-73.936854"),
        Coordinate("40.759874", "-73.936801"),
        Coordinate("40.759889", "-73.936787"),
        Coordinate("40.759836", "-73.936675"),
        Coordinate("40.759504", "-73.935966"),
        Coordinate("40.759466", "-73.935882"),
        Coordinate("40.759436", "-73.935814"),
        Coordinate("40.759275", "-73.935484"),
        Coordinate("40.759106", "-73.935157"),
        Coordinate("40.759064", "-73.935066"),
        Coordinate("40.759034", "-73.935002"),
        Coordinate("40.758702", "-73.93426")
    ).let {
        Route(
            "1",
            "1",
            1,
            "S0",
            "S1",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val routeS1S2 = listOf(
        Coordinate("40.758702", "-73.93426"),
        Coordinate("40.758639", "-73.934309"),
        Coordinate("40.75797", "-73.934922")
    ).let {
        Route(
            "1",
            "2",
            1,
            "S1",
            "S2",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val routeS2S3 = listOf(
        Coordinate("40.75797", "-73.934922"),
        Coordinate("40.757489", "-73.935363"),
        Coordinate("40.757451", "-73.935389"),
        Coordinate("40.757393", "-73.935465"),
        Coordinate("40.757354", "-73.935542"),
        Coordinate("40.756611", "-73.936215"),
        Coordinate("40.756326", "-73.936473"),
        Coordinate("40.756277", "-73.936518"),
        Coordinate("40.756212", "-73.936581"),
        Coordinate("40.756152", "-73.936636")
    ).let {
        Route(
            "1",
            "3",
            1,
            "S2",
            "S3",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val routeS3S4 = listOf(
        Coordinate("40.756152", "-73.936636"),
        Coordinate("40.755221", "-73.937493"),
        Coordinate("40.75516", "-73.937549"),
        Coordinate("40.755127", "-73.937485")
    ).let {
        Route(
            "1",
            "4",
            1,
            "S3",
            "S4",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    /// **
    val routeS4S5 = listOf(
        Coordinate("40.755127", "-73.937485"),
        Coordinate("40.75265", "-73.93982")
    ).let {
        Route(
            "1",
            "5",
            1,
            "S4",
            "S5",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    // **
//    val routeS4S0 = listOf(
//        Coordinate("40.755127", "-73.937485"),
//        Coordinate("40.755997", "-73.940509")
//    ).let {
//        Route(
//            "1",
//            "5",
//            1,
//            "S4",
//            "S0",
//            coordinates = it,
//            Duration.ZERO,
//            "d2e2d2e"
//        )
//    }

    val busStops = listOf(
        BusStop(
            stopId = "S0",
            coordinates = routeS0S1.coordinates.first()
        ),
        BusStop(
            stopId = "S1",
            coordinates = routeS0S1.coordinates.last()
        ),
        BusStop(
            stopId = "S2",
            coordinates = routeS1S2.coordinates.last()
        ),
        BusStop(
            stopId = "S3",
            coordinates =  routeS2S3.coordinates.last()
        ),
        BusStop(
            stopId = "S4",
            coordinates =  routeS3S4.coordinates.last()
        ),
        //***
        BusStop(
            stopId = "S5",
            coordinates =  routeS4S5.coordinates.last()
        )
    )

    var r  =  generateRecentCoordinatesFromSegment(routeS4S5.coordinates.reversed(),busStops).map { it.second }
    println(r)


    val result = BusRouteAndStopDiscoverer().findNextLastStopAndIfIsReturningFromScratch(
        RouteType.OutAndBack,  //// it cant be loop because in the route list there is no route between first and last stop.
        listOf(routeS0S1, routeS1S2, routeS2S3, routeS3S4, routeS4S5),
        busStops,
       r
    )
    println(formatCoordinatesForExport( r,"#FFFF00"))
    println("Distance between S2 and S3 = ${TurfMeasurement.distance(busStops[2].coordinates.toPoint(),busStops[3].coordinates.toPoint(), TurfConstants.UNIT_METERS)}")
    println("Distance between S3 and and last interpolated point  = ${TurfMeasurement.distance(Coordinate("40.757083818182","-73.935786727273").toPoint(),busStops[3].coordinates.toPoint(), TurfConstants.UNIT_METERS)}")
    println("Distance between S2 and and first interpolated point  = ${TurfMeasurement.distance(Coordinate("40.75721890909091","-73.93566436363636").toPoint(),busStops[2].coordinates.toPoint(), TurfConstants.UNIT_METERS)}")


    //println("Distance between S4 and and last interpolated point  = ${TurfMeasurement.distance(Coordinate("40.7557795","-73.939753").toPoint(),busStops[0].coordinates.toPoint(), TurfConstants.UNIT_METERS)}")
    println("Distance between S5 and and last interpolated point  = ${TurfMeasurement.distance(Coordinate("40.753451382353","-73.939064558824").toPoint(),busStops[5].coordinates.toPoint(), TurfConstants.UNIT_METERS)}")



    println("Size == ${r.size}")
    println("Result is = $result")
}





























/// TESTING FLINK JOB
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

private const val BUS_LOCATION_DATA_TOPIC = "BUS_LOCATION_DATA"
private const val BUS_DATA_TOPIC = "BUS_DATA"
private const val BUS_ROUTES_DATA_TOPIC = "BUS_ROUTES_DATA"


/// Kafka Sink Topics
private const val BUS_ETA_DATA_TOPIC = "BUS_ETA_DATA"
private const val BUS_TRACKING_DATA_TOPIC = "BUS_TRACKING_DATA"


object KafkaBusSender {            // keep one instance
    private val producer = KafkaProducer<String, String>(
        Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.29.70:9092")
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner")
        }
    )

    fun send(topic: String, value: String) {
        producer.send(ProducerRecord(topic, null, value)) { m, e ->
            if (e == null) println("Sent to topic ${m.topic()} offset ${m.offset()} partition ${m.partition()}")
            else e.printStackTrace()
        }
    }

    fun close() = producer.close()
}




@OptIn(ExperimentalTime::class)
fun main(){

    val json = Json
    val busData = BusData(
        busId = "1",
        routeType = RouteType.OutAndBack,
        stops = listOf(
            BusStop(
                stopId = "S0",
                coordinates = Coordinate("40.755997", "-73.940509")
            ),
            BusStop(
                stopId = "S1",
                coordinates = Coordinate("40.758702", "-73.93426")
            ),
            BusStop(
                stopId = "S2",
                coordinates = Coordinate("40.75797", "-73.934922")
            ),
            BusStop(
                stopId = "S3",
                coordinates = Coordinate("40.756152", "-73.936636")
            ),
            BusStop(
                stopId = "S4",
                coordinates = Coordinate("40.755127", "-73.937485")
            ),
            BusStop(
                stopId = "S5",
                coordinates = Coordinate("40.75265", "-73.93982")
            )
        )
    )

    val routeS0S1 = listOf(
        Coordinate("40.755997", "-73.940509"),
        Coordinate("40.75629", "-73.940229"),
        Coordinate("40.756307", "-73.940212"),
        Coordinate("40.756487", "-73.94004"),
        Coordinate("40.756835", "-73.93972"),
        Coordinate("40.756886", "-73.939673"),
        Coordinate("40.75693", "-73.939633"),
        Coordinate("40.756985", "-73.939582"),
        Coordinate("40.757432", "-73.939163"),
        Coordinate("40.757503", "-73.939097"),
        Coordinate("40.757573", "-73.939028"),
        Coordinate("40.758006", "-73.9386"),
        Coordinate("40.758404", "-73.938217"),
        Coordinate("40.758615", "-73.938014"),
        Coordinate("40.758652", "-73.937978"),
        Coordinate("40.758722", "-73.937911"),
        Coordinate("40.758785", "-73.93785"),
        Coordinate("40.759258", "-73.937391"),
        Coordinate("40.759403", "-73.93725"),
        Coordinate("40.759605", "-73.937056"),
        Coordinate("40.759817", "-73.936854"),
        Coordinate("40.759874", "-73.936801"),
        Coordinate("40.759889", "-73.936787"),
        Coordinate("40.759836", "-73.936675"),
        Coordinate("40.759504", "-73.935966"),
        Coordinate("40.759466", "-73.935882"),
        Coordinate("40.759436", "-73.935814"),
        Coordinate("40.759275", "-73.935484"),
        Coordinate("40.759106", "-73.935157"),
        Coordinate("40.759064", "-73.935066"),
        Coordinate("40.759034", "-73.935002"),
        Coordinate("40.758702", "-73.93426")
    ).let {
        Route(
            "1", "1", 1, "S0", "S1", coordinates = it,
            duration = (70L).seconds,
            distanceInMeters = "785.30"
        )
    }

    val routeS1S2 = listOf(
        Coordinate("40.758702", "-73.93426"),
        Coordinate("40.758639", "-73.934309"),
        Coordinate("40.75797", "-73.934922")
    ).let {
        Route(
            "1", "2", 1, "S1", "S2", coordinates = it,
            duration = (8L).seconds,
            distanceInMeters = "98.67"
        )
    }

    val routeS2S3 = listOf(
        Coordinate("40.75797", "-73.934922"),
        Coordinate("40.757489", "-73.935363"),
        Coordinate("40.757451", "-73.935389"),
        Coordinate("40.757393", "-73.935465"),
        Coordinate("40.757354", "-73.935542"),
        Coordinate("40.756611", "-73.936215"),
        Coordinate("40.756326", "-73.936473"),
        Coordinate("40.756277", "-73.936518"),
        Coordinate("40.756212", "-73.936581"),
        Coordinate("40.756152", "-73.936636")
    ).let {
        Route(
            "1", "3", 1, "S2", "S3", coordinates = it,
            duration = (22).seconds,
            distanceInMeters = "249.11"
        )
    }

    val routeS3S4 = listOf(
        Coordinate("40.756152", "-73.936636"),
        Coordinate("40.755221", "-73.937493"),
        Coordinate("40.75516", "-73.937549"),
        Coordinate("40.755127", "-73.937485")
    ).let {
        Route(
            "1", "4", 1, "S3", "S4", coordinates = it,
            duration = (12L).seconds,
            distanceInMeters = "140.99"
        )
    }

    val routeS4S5 = listOf(
        Coordinate("40.755127", "-73.937485"),
        Coordinate("40.75265", "-73.93982")
    ).let {
        Route(
            "1", "5", 1, "S4", "S5", coordinates = it,
            duration = (30L).seconds,
            distanceInMeters = "338.44"
        )
    }

    var r  =  generateRecentCoordinatesFromSegment(routeS0S1.coordinates,busData.stops)

    println(r)

    val p = KafkaBusSender

////

//    runBlocking {
//        p.send(BUS_DATA_TOPIC,json.encodeToString(busData)) /// sent
//        p.send(BUS_ROUTES_DATA_TOPIC,json.encodeToString(routeS0S1)) /// sent
//        p.send(BUS_ROUTES_DATA_TOPIC,json.encodeToString(routeS1S2)) /// sent
//        p.send(BUS_ROUTES_DATA_TOPIC,json.encodeToString(routeS2S3)) /// sent
//        p.send(BUS_ROUTES_DATA_TOPIC,json.encodeToString(routeS3S4)) /// sent
//        p.send(BUS_ROUTES_DATA_TOPIC,json.encodeToString(routeS4S5)) /// sent
//    }






//
    runBlocking {
        repeat(100) {
            r.forEach {
                delay(1000)
                p.send(topic = BUS_LOCATION_DATA_TOPIC, value = json.encodeToString(BusLocationData(
                    busId = "1",
                    coordinate = it.second,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                )))
            }
        }

    }

}


////////////////////////////////////////////////////////////////////////////////////////////////////////
































///// get point in route // SUCCESS
/*fun main(){

    val route1 = listOf(
        Coordinate("40.755997", "-73.940509"),
        Coordinate("40.75629", "-73.940229"),
        Coordinate("40.756307", "-73.940212"),
        Coordinate("40.756487", "-73.94004"),
        Coordinate("40.756835", "-73.93972"),
        Coordinate("40.756886", "-73.939673"),
        Coordinate("40.75693", "-73.939633"),
        Coordinate("40.756985", "-73.939582"),
        Coordinate("40.757432", "-73.939163"),
        Coordinate("40.757503", "-73.939097"),
        Coordinate("40.757573", "-73.939028"),
        Coordinate("40.758006", "-73.9386"),
        Coordinate("40.758404", "-73.938217"),
        Coordinate("40.758615", "-73.938014"),
        Coordinate("40.758652", "-73.937978"),
        Coordinate("40.758722", "-73.937911"),
        Coordinate("40.758785", "-73.93785"),
        Coordinate("40.759258", "-73.937391"),
        Coordinate("40.759403", "-73.93725"),
        Coordinate("40.759605", "-73.937056"),
        Coordinate("40.759817", "-73.936854"),
        Coordinate("40.759874", "-73.936801"),
        Coordinate("40.759889", "-73.936787"),
        Coordinate("40.759836", "-73.936675"),
        Coordinate("40.759504", "-73.935966"),
        Coordinate("40.759466", "-73.935882"),
        Coordinate("40.759436", "-73.935814"),
        Coordinate("40.759275", "-73.935484"),
        Coordinate("40.759106", "-73.935157"),
        Coordinate("40.759064", "-73.935066"),
        Coordinate("40.759034", "-73.935002"),
        Coordinate("40.758702", "-73.93426")
    ).let {
        Route(
            "1",
            "1",
            1,
            "S1",
            "S2",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val route2 = listOf(
        Coordinate("40.758702", "-73.93426"),
        Coordinate("40.758639", "-73.934309"),
        Coordinate("40.75797", "-73.934922")
    ).let {
        Route(
            "1",
            "2",
            1,
            "S2",
            "S3",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val route3 = listOf(
        Coordinate("40.75797", "-73.934922"),
        Coordinate("40.757489", "-73.935363"),
        Coordinate("40.757451", "-73.935389"),
        Coordinate("40.757393", "-73.935465"),
        Coordinate("40.757354", "-73.935542"),
        Coordinate("40.756611", "-73.936215"),
        Coordinate("40.756326", "-73.936473"),
        Coordinate("40.756277", "-73.936518"),
        Coordinate("40.756212", "-73.936581"),
        Coordinate("40.756152", "-73.936636")
    ).let {
        Route(
            "1",
            "3",
            1,
            "S3",
            "S4",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val route4 = listOf(
        Coordinate("40.756152", "-73.936636"),
        Coordinate("40.755221", "-73.937493"),
        Coordinate("40.75516", "-73.937549"),
        Coordinate("40.755127", "-73.937485")
    ).let {
        Route(
            "1",
            "4",
            1,
            "S4",
            "S5",
            coordinates = it,
            Duration.ZERO,
            "d2e2d2e"
        )
    }

    val busStops = listOf(
        BusStop(
            stopId = "S1",
            coordinates = route1.coordinates.first()
        ),
        BusStop(
            stopId = "S2",
            coordinates = route1.coordinates.last()
        ),
        BusStop(
            stopId = "S3",
            coordinates = route2.coordinates.last()
        ),
        BusStop(
            stopId = "S4",
            coordinates =  route3.coordinates.last()
        ),
        BusStop(
            stopId = "S5",
            coordinates =  route4.coordinates.last()
        )
    )


//    val currentPoint = Coordinate(
//        latitude = (40.7552).toString(),
//        longitude = (-73.93743).toString()
//    )
//    val currentPoint = Coordinate(
//        latitude = (40.75642).toString(),
//        longitude = (-73.93994).toString()
//    )

//    val currentPoint = Coordinate(
//        latitude = (40.75828).toString(),
//        longitude = (-73.93453).toString()
//    )

    val currentPoint =  Coordinate("40.756152", "-73.936636")

    val routeId = BusPathDiscovery().getPointInRouteFromScratch(
        currentPoint,
        listOf(route1, route2, route3, route4).reversed()
    )

    val routeId2 = BusPathDiscovery().getPointInRoute(
        currentPoint,
        "3",
        "4",
        listOf(route1, route2, route3, route4)
    )

    print("1 -- "+routeId+"\n")
    print("2 -- "+routeId2)
}*/






/////  Testing Nearest point on line // Success
/*
fun main (){

    val point0 =  Point.fromLngLat(-73.940587,40.756040)
    val point1 =  Point.fromLngLat(-73.934217,40.758699)
    val point2 =  Point.fromLngLat(-73.934841,40.757927)
    val point3 = Point.fromLngLat(-73.936152,40.756578)
    val point4 = Point.fromLngLat(-73.937480,40.755128)
    val point5 = Point.fromLngLat(-73.938820,40.754864)
    val point6 = Point.fromLngLat(-73.939563,40.755424)

//    val polyline = LineString.fromLngLats(listOf(
//    ))

   // val point  = Point.fromLngLat(-73.94123,40.7553)

    //val point = Point.fromLngLat(-73.94126,40.75527)

    //val point = Point.fromLngLat(-73.94117,40.75535)

   // val point = Point.fromLngLat(-73.94030,40.75472)
    val point = Point.fromLngLat(-73.938820,40.754864)

    val nearest = TurfMisc.nearestPointOnLine(point,listOf(point0,point1,point2,point3,point4, point5,point6),TurfConstants.UNIT_METERS)
    println(nearest)

    val nearestCoord = Coordinate(
        (nearest.geometry() as Point).coordinates().last().toString(),
        (nearest.geometry() as Point).coordinates().first().toString()
    )
    val distanceToNearestCoord = nearest.properties()!!.get("dist").toString()
    println(nearestCoord)
    println(distanceToNearestCoord)

    val distance  = TurfMeasurement.distance(
        point,Point.fromLngLat(-73.94119447265167,40.75528046331999),
        TurfConstants.UNIT_METERS
    )

    println(distance)
}
*/










////// Testing ETA // Success
/*

fun geoJsonToCoordinates() = listOf(
    Coordinate(latitude = "40.755997", longitude = "-73.940509"),
    Coordinate(latitude = "40.75629",  longitude = "-73.940229"),
    Coordinate(latitude = "40.756307", longitude = "-73.940212"),
    Coordinate(latitude = "40.756487", longitude = "-73.94004"),
    Coordinate(latitude = "40.756835", longitude = "-73.93972"),
    Coordinate(latitude = "40.756886", longitude = "-73.939673"),
    Coordinate(latitude = "40.75693",  longitude = "-73.939633"),
    Coordinate(latitude = "40.756985", longitude = "-73.939582"),
    Coordinate(latitude = "40.757432", longitude = "-73.939163"),
    Coordinate(latitude = "40.757503", longitude = "-73.939097"),
    Coordinate(latitude = "40.757573", longitude = "-73.939028"),
    Coordinate(latitude = "40.758006", longitude = "-73.9386"),
    Coordinate(latitude = "40.758404", longitude = "-73.938217"),
    Coordinate(latitude = "40.758615", longitude = "-73.938014"),
    Coordinate(latitude = "40.758652", longitude = "-73.937978"),
    Coordinate(latitude = "40.758722", longitude = "-73.937911"),
    Coordinate(latitude = "40.758785", longitude = "-73.93785"),
    Coordinate(latitude = "40.759258", longitude = "-73.937391"),
    Coordinate(latitude = "40.759403", longitude = "-73.93725"),
    Coordinate(latitude = "40.759605", longitude = "-73.937056"),
    Coordinate(latitude = "40.759817", longitude = "-73.936854"),
    Coordinate(latitude = "40.759874", longitude = "-73.936801"),
    Coordinate(latitude = "40.759889", longitude = "-73.936787"),
    Coordinate(latitude = "40.759836", longitude = "-73.936675"),
    Coordinate(latitude = "40.759504", longitude = "-73.935966"),
    Coordinate(latitude = "40.759466", longitude = "-73.935882"),
    Coordinate(latitude = "40.759436", longitude = "-73.935814"),
    Coordinate(latitude = "40.759275", longitude = "-73.935484"),
    Coordinate(latitude = "40.759106", longitude = "-73.935157"),
    Coordinate(latitude = "40.759064", longitude = "-73.935066"),
    Coordinate(latitude = "40.759034", longitude = "-73.935002"),
    Coordinate(latitude = "40.758702", longitude = "-73.93426")
)


@OptIn(ExperimentalTime::class)
fun main(){
    val calculator = EtaCalculator()

    val currentStop = FromStopDetails(
        "40.755997",
        "-73.940509",
        Duration.ZERO,
        null
    )
//
//    val currentStop = FromStopDetails(
//        "40.759106",
//        "-73.935157",
//        null
//    )

    val route  = CurrentRouteDetails(
        "BUS_001",
        "ROUTE_001",
        geoJsonToCoordinates(),
        (142L).seconds,
        "785.776",
        RouteType.OutAndBack,
        true
    )

    val oldest  = TimeStampedCoordinate(
        coordinate = Coordinate(latitude = "40.755997", longitude = "-73.940509"),
        Clock.System.now().toEpochMilliseconds()
    )

    val latest = TimeStampedCoordinate(
        coordinate = Coordinate(latitude = "40.755997", longitude = "-73.940509"),
        Clock.System.now().toEpochMilliseconds()
    )

    val result  = calculator.calculateEta(
       // (100L).seconds,
        oldest,
        latest,
        route,
        currentStop,
    )

    when(result) {
        is EtaResult.Ahead -> println("ETA is ${result.etaInSec} seconds.\nRemaining Distance = ${result.remainingDistance.toDouble().toInt()} meters.\nAhead by ${result.deltaInSec} seconds. ")
        is EtaResult.Delayed -> println("ETA is ${result.etaInSec} seconds.\nRemaining Distance = ${result.remainingDistance.toDouble().toInt()} meters.\nDelayed by ${result.deltaInSec} seconds. " +
                " ")
        null -> println("ETA is null")
    }
}
*/

















//// Testing ETA 2
//fun main(){
//    val busLocationWithMetadataWindowed = BusLocationWithMetadataWindowed(
//        busId = "1",
//        busLocationMetadata = BusLocationWithMetadata(
//            busId = "1",
//            currentRoute = Route(
//                busId = "1",
//                routeId = "1",
//                routeCount = 1,
//                fromStopId = "S0",
//                toStopId = "S1",
//                coordinates = listOf(
//                    Coordinate("40.755997", "-73.940509"),
//                    Coordinate("40.75629", "-73.940229"),
//                    Coordinate("40.756307", "-73.940212"),
//                    Coordinate("40.756487", "-73.94004"),
//                    Coordinate("40.756835", "-73.93972"),
//                    Coordinate("40.756886", "-73.939673"),
//                    Coordinate("40.75693", "-73.939633"),
//                    Coordinate("40.756985", "-73.939582"),
//                    Coordinate("40.757432", "-73.939163"),
//                    Coordinate("40.757503", "-73.939097"),
//                    Coordinate("40.757573", "-73.939028"),
//                    Coordinate("40.758006", "-73.9386"),
//                    Coordinate("40.758404", "-73.938217"),
//                    Coordinate("40.758615", "-73.938014"),
//                    Coordinate("40.758652", "-73.937978"),
//                    Coordinate("40.758722", "-73.937911"),
//                    Coordinate("40.758785", "-73.93785"),
//                    Coordinate("40.759258", "-73.937391"),
//                    Coordinate("40.759403", "-73.93725"),
//                    Coordinate("40.759605", "-73.937056"),
//                    Coordinate("40.759817", "-73.936854"),
//                    Coordinate("40.759874", "-73.936801"),
//                    Coordinate("40.759889", "-73.936787"),
//                    Coordinate("40.759836", "-73.936675"),
//                    Coordinate("40.759504", "-73.935966"),
//                    Coordinate("40.759466", "-73.935882"),
//                    Coordinate("40.759436", "-73.935814"),
//                    Coordinate("40.759275", "-73.935484"),
//                    Coordinate("40.759106", "-73.935157"),
//                    Coordinate("40.759064", "-73.935066"),
//                    Coordinate("40.759034", "-73.935002"),
//                    Coordinate("40.758702", "-73.93426")
//                ),
//                duration = Duration.ZERO,
//                distanceInMeters = "785.30"
//            ),
//            routeType = RouteType.OutAndBack,
//            location = TimeStampedCoordinate(
//                coordinate = Coordinate("40.757634855734636", "-73.93896685853481"),
//                timestamp = 1749900955524
//            ),
//            isReturning = false,
//            currentStop = null,
//            lastPassedStop = Pair(
//                null,
//                BusStop(
//                    stopId = "S0",
//                    coordinates = Coordinate("40.755997", "-73.940509"),
//                    waitTime = Duration.ZERO
//                )
//            ),
//            nextStop = BusStop(
//                stopId = "S1",
//                coordinates = Coordinate("40.758702", "-73.93426"),
//                waitTime = Duration.ZERO
//            )
//        ),
//        oldestCoordinates = TimeStampedCoordinate(
//            coordinate = Coordinate("40.757634855734636", "-73.93896685853481"),
//            timestamp = 1749900955524
//        ),
//        latestCoordinates = TimeStampedCoordinate(
//            coordinate = Coordinate("40.75775857080269", "-73.93884457204722"),
//            timestamp = 1749900955524
//        )
//    )
//
//
//    val currRoute = busLocationWithMetadataWindowed.busLocationMetadata.currentRoute
//
//    val currentRouteDetails = CurrentRouteDetails(
//        busId = busLocationWithMetadataWindowed.busId,
//        routeId = currRoute.routeId,
//        coordinates = currRoute.coordinates,
//        duration = currRoute.duration,
//        distanceInMeters = currRoute.distanceInMeters,
//        routeType = busLocationWithMetadataWindowed.busLocationMetadata.routeType,
//        isReturning = busLocationWithMetadataWindowed.busLocationMetadata.isReturning,
//    )
//
//    val lastPassedStop = busLocationWithMetadataWindowed.busLocationMetadata.lastPassedStop.second
//    val lastPassedArrivalTime = busLocationWithMetadataWindowed.busLocationMetadata.lastPassedStop.first?.milliseconds
//
//    val fromStopDetails = FromStopDetails(
//        latitude = lastPassedStop.coordinates.latitude,
//        longitude = lastPassedStop.coordinates.longitude,
//        waitTime = lastPassedStop.waitTime,
//        arrivalTime = lastPassedArrivalTime
//    )
//
//    val etaCal = EtaCalculator()
//
//    val r = etaCal.calculateEta(
//       oldestCoordinates = busLocationWithMetadataWindowed.oldestCoordinates,
//        latestCoordinate = busLocationWithMetadataWindowed.latestCoordinates,
//        currentRoute = currentRouteDetails,
//        fromStop = fromStopDetails
//    )
//
//
//    println(r)
//
//}