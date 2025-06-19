package models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

// TODO("Add Service")

@Serializable
data class Route(
    val routeId: String,
    val routeCount: Int,
    val fromStopId: String,
    val toStopId: String,
    val coordinates: List<Coordinate>,
    val duration: Duration,
    val distanceInMeters: String,
)