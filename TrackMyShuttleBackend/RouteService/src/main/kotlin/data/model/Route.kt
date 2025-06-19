package org.example.data.model

import data.entity.RouteEntity
import kotlin.time.Duration

data class Route(
    val routeId: String,
    val routeCount: Int, /// remove route count
    val fromStopId: String,
    val toStopId: String,
    val coordinates: List<Coordinate>,
    val duration: Duration,
    val distanceInMeters: Double,
)

fun Route.toRouteEntity(): RouteEntity {
    return RouteEntity(
        routeId = routeId,
        routeCount = routeCount,
        fromStopId = fromStopId,
        toStopId = toStopId,
        distanceInMeters = distanceInMeters,
        duration = duration,
        coordinates = coordinates,
    )
}