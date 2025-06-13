package models

import kotlinx.serialization.Serializable
import util.RouteType

@Serializable
data class BusTrackingData(
    val busId: String,
    val currentRouteId: String ,
    val routeType: RouteType,
    val location: TimeStampedCoordinate,
    val isReturning: Boolean,
    val currentStopId: String?, // if reached its destination
    val lastPassedStopId: String,
    val nextStopId: String,
)