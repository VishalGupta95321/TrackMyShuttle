package models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class BusStopInfo(
    val stopId: String,
    val coordinates: Coordinates,
    val waitTime: Duration,
)

@Serializable
data class BusStopsData(
    val busId : String,
    val stops : List<BusStopInfo>
)
