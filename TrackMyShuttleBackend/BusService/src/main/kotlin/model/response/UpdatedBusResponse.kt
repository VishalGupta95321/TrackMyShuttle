package model.response

import kotlinx.serialization.Serializable
import util.CustomBusStatusDtoSerializer

@Serializable
data class UpdatedBusResponse(
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
)