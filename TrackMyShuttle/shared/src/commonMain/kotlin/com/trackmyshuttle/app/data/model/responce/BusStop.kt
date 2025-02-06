package com.trackmyshuttle.app.data.model.responce

import kotlinx.serialization.Serializable

@Serializable
data class BusStop(
    val id: String,
    val name: String,
    val latitude: String,
    val longitude: String
)