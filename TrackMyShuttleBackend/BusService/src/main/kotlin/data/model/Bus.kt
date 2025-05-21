package data.model

import data.entity.BusEntity

data class Bus(
    val busId: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatus?,
    val stopIds: List<String>,
    val currentStop: String?,  // these two fields will be added by server or wherever
    val nextStop: String?,   //
)

fun Bus.toBusEntity(
    partitionKey: String,
): BusEntity = BusEntity(
    busId = busId,
    partitionKey = partitionKey,
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
    busStatus = busStatus,
    stopIds = null,
    currentStop = currentStop,
    nextStop = nextStop,
    )