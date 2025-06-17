package model.request

import data.model.BasicBusDetails
import data.model.Bus
import data.util.RouteType
import kotlinx.serialization.Serializable
import model.RouteTypeDto

@Serializable
data class BusRegistrationRequest(
    val busId: String,
    val routeType: RouteTypeDto,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
)

fun BusRegistrationRequest.toBus() = Bus(
    busId = busId,
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
    busStatus = null,
    stopIds = emptyList(),
    currentStop = null,
    routeType = routeType.toRouteType(),
    nextStop = null,
)

fun BusRegistrationRequest.toBasicBus() = BasicBusDetails(
    routeType = routeType.toRouteType(),
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
)
