package models

import kotlinx.serialization.Serializable
import util.WaitTime
import kotlin.time.Duration

@Serializable
data class RawBusStop(
    val stopId: String,
    val coordinates: Coordinate,
    val stopRadiusInMeters: Double,
)

fun RawBusStop.toBusStop(waitTime: WaitTime): BusStop {
    return BusStop(
        stopId = stopId,
        coordinates = coordinates,
        waitTime = waitTime,
        stopRadiusInMeters = stopRadiusInMeters
    )
}

@Serializable
data class BusStop(
    val stopId: String,
    val coordinates: Coordinate,
    val waitTime: Duration = Duration.ZERO,
    val stopRadiusInMeters: Double,
)
