package model.request

import data.model.BusStatus
import kotlinx.serialization.Serializable
import model.BusStatusDto
import util.CustomBusStatusDtoSerializer

@Serializable
data class BusStatusUpdateRequest(
    val busStatus: BusStatusDto,
)
