package com.trackmyshuttle.app.data.model.responce

import kotlinx.serialization.Serializable

@Serializable
data class Bus(
    val busId: String,
    val driverName: String,
    val busStops: List<BusStop>,
    val isActive: Boolean,
    val activeHours: String,
    val activeDays: String
)