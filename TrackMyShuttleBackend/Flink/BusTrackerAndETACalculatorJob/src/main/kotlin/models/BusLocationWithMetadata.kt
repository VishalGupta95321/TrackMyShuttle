package models

import kotlinx.serialization.Serializable
import util.RouteType
import util.TimeStamp


@Serializable
data class BusLocationWithMetadata(
    val busId: String,
    val currentRoute: Route,
    val routeType: RouteType,
    val location: TimeStampedCoordinate,
    val isReturning: Boolean,
    val currentStop: BusStop?, // if reached its destination
    val lastPassedStop: Pair<TimeStamp?,BusStop>, // Timestamp = When it passed the stop.
    val nextStop: BusStop,
)

fun BusLocationWithMetadata.toBusTrackingData() = BusTrackingData(
    busId = busId,
    currentRouteId = currentRoute.routeId,
    routeCount = currentRoute.routeCount,
    routeType = routeType,
    location = location,
    isReturning = isReturning,
    lastPassedStopId = lastPassedStop.second.stopId,
    nextStopId = nextStop.stopId,
    currentStopId = currentStop?.stopId,
)