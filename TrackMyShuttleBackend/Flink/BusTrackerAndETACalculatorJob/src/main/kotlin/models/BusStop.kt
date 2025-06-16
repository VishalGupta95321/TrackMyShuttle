package models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class BusStop(
    val stopId: String,
    val coordinates: Coordinate,
    val waitTime: Duration = Duration.ZERO, // TODO("Update Service")
    val stopRadiusInMeters: Double,  // TODO("Update Service")
)
