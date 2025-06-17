package data.model

import data.util.RouteType

data class BasicBusDetails(
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val routeType: RouteType,
)