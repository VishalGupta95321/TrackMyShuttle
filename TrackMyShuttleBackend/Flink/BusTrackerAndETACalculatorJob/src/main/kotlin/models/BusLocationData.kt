package models

import kotlinx.serialization.Serializable

@Serializable
data class BusLocationData(
    val busId : String,
    val coordinate : Coordinate,
    val timestamp : Long
)