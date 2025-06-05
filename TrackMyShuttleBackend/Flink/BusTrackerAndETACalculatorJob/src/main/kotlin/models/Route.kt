package models

import kotlin.time.Duration

data class Route(
    val busId: String,
    val routeNo: String,
    val coordinates: List<Coordinates>,
    val duration: Duration,
    val distanceInMeters: String
)