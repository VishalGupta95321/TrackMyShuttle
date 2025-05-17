package model.request

import data.model.Bus
import model.BusStatusDto


data class BusRegistrationOrUpdateRequest(
    val busId: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatusDto?,
)

fun BusRegistrationOrUpdateRequest.toBus() = Bus(
    busId = busId,
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
    busStatus = busStatus?.toBusStatus(),
    stopIds = emptyList(),
    currentStop = null,
    nextStop = null,
)