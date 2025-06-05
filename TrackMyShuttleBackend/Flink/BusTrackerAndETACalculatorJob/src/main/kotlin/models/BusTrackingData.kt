package models

import kotlinx.serialization.Serializable

@Serializable
data class BusTrackingData(
    val currentStop: BusStopInfo,
    val nestStop: BusStopInfo,
    val distanceToNxtStop: String,
    val etaToNxtStop: String,
)