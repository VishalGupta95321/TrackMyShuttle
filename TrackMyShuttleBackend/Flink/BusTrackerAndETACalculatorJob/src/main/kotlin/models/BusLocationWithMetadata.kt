package models

import util.RouteType

typealias Timestamp = Long
data class BusLocationWithMetadata(
    val busId: String,
    val currentRoute: Route,
    val routeType: RouteType,
    val location: Pair<Timestamp,Coordinate>,
    val isReturning: Boolean,
    val currentStop: BusStop?, // if reached its destination
    val lastPassedStop: Pair<Timestamp?,BusStop>, // Timestamp = When it passed the stop.
    val nextStop: BusStop,
)