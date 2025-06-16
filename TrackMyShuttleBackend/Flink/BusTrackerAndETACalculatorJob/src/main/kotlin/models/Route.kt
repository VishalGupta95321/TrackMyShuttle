package models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

// TODO("Add Service")

@Serializable
data class Route(
   // val busId: String,
    val routeId: String,    /// StopIdA + StopIdB // Ok Ok Not because there gonna be multiple routes between two stops
    val routeCount: Int,  ///////// There could be more than one route between two points.
    val fromStopId: String,
    val toStopId: String,
    val coordinates: List<Coordinate>,
    val duration: Duration,
    val distanceInMeters: String, // Route Length
)