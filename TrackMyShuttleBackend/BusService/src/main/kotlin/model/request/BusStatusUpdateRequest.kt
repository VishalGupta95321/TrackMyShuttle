package model.request

import data.model.BusStatus
import model.BusStatusDto

data class BusStatusUpdateRequest(
    val busId: String,
    val busStatus: BusStatusDto,
)