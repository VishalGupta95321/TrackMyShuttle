package model.request

import model.BusStatusDto


data class BusRegistrationRequest(
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatusDto,
    val stopsIds: List<String>,
)

