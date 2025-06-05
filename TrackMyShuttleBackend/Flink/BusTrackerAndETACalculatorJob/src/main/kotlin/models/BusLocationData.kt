package models

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val latitude: String,
    val longitude: String,
    val timeStamp: String,
)

@Serializable
data class BusLocationData(
    val busId : String,
    val coordinates : Coordinates,
)