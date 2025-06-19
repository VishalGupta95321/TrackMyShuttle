package org.example.data.model.mapbox

import kotlinx.serialization.Serializable

@Serializable
data class DirectionsResponse(
    val routes: List<RouteDto>, /// can be multiple
)

@Serializable
data class RouteDto(
    val duration: Double,
    val distance: Double,
    val geometry: GeometryDto,
)

@Serializable
data class GeometryDto(
    val type: String,
    val coordinates: List<List<Double>>
)