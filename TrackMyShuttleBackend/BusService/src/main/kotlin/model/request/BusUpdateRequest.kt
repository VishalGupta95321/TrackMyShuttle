package model.request

import data.model.BasicBusDetails
import kotlinx.serialization.Serializable
import util.CustomBusStatusDtoSerializer

@Serializable
data class BusUpdateRequest(
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
){
    fun toBasicBus() = BasicBusDetails(
        driverName,
        activeHours,
        activeDays
    )
}