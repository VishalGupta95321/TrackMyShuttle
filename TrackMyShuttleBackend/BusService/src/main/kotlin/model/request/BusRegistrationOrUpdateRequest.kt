package model.request

import model.BusStatusDto


data class BusRegistrationOrUpdateRequest(
    val busId: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatusDto,
    val stopsIds: List<String>,
)

