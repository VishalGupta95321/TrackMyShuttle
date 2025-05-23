package model.request

import model.response.BusStopDto

data class UpdateBusStopRequest(
    val stop: BusStopDto
)