package model.request

import model.BusStatusDto

data class BusStatusUpdateRequest(
    val busId: String,
    val status: BusStatusDto
)