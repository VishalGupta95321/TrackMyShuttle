package com.trackmyshuttle.app.data.model.responce

import kotlinx.serialization.Serializable

@Serializable
data class BusLocationData(
    val busId: String,
    val latitude: Double,
    val longitude: Double,
    val eta: Float,
    val distance: Float
)