package models

import kotlinx.serialization.Serializable

@Serializable
data class BusTrackingData(
    val currentStop: BusStop,
    val nextStop: BusStop,
    val distanceToNxtStop: String,
    val etaToNxtStop: String,
)