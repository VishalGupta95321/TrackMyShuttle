package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: String,
    val longitude: String
)
