package models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class BusStop(
    val stopId: String,
    val coordinates: Coordinate,
    val waitTime: Duration = Duration.ZERO,
    //val stopRadiusInMeters: Double,   //// TODO Maybe do this and update the code accordingly
)
