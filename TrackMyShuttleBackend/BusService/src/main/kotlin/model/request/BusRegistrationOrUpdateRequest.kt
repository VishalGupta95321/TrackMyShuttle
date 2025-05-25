package model.request

import data.model.BasicBusDetails
import data.model.Bus


data class BusRegistrationOrUpdateRequest(
    val busId: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
)

fun BusRegistrationOrUpdateRequest.toBus() = Bus(
    busId = busId,
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
    busStatus = null,
    stopIds = emptyList(),
    currentStop = null,
    nextStop = null,
)

fun BusRegistrationOrUpdateRequest.toBasicBus() = BasicBusDetails(
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
)
