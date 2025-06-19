package org.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(
    val latitude: String,
    val longitude: String,
)
