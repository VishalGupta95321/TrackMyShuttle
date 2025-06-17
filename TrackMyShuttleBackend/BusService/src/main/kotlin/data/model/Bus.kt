package data.model

import data.entity.BusEntity
import data.util.RouteType

data class Bus(
    val busId: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatus?,
    val stopIds: List<StopIdsWithWaitTime>,
    val routeType: RouteType,
    val currentStop: String?,  // these two fields will be added by server or wherever
    val nextStop: String?,   //
)

fun Bus.toBusEntity(): BusEntity = BusEntity(
    busId = busId,
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
    busStatus = busStatus,
    stopIds = listOf(),
    currentStop = currentStop,
    nextStop = nextStop,
    routeType = routeType
)