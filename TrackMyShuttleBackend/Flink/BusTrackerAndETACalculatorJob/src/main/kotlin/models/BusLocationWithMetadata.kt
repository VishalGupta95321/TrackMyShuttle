package models

import util.RouteType

data class BusLocationWithMetadata(
    val busId: String,
    val routeId: String,
    val routeType: RouteType,
    val currentLocation: Coordinate,
    val isReturning: Boolean,
    val currentStop: BusStop?, // if reached its destination
    val lastPassedStop: BusStop,
    val nextStop: BusStop,
)