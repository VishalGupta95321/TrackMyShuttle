package model.request

import data.model.BasicBusDetails
import data.model.Bus
import kotlinx.serialization.Serializable

@Serializable
data class BusRegistrationRequest(
    val busId: String,
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
    nextStop = null,
)

fun BusRegistrationRequest.toBasicBus() = BasicBusDetails(
    driverName = driverName,
    activeHours = activeHours,
    activeDays = activeDays,
)
