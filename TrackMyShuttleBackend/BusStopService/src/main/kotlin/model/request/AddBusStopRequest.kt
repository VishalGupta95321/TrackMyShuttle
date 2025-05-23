package model.request

import data.model.BusStop
import model.response.BusStopDto

data class AddBusStopRequest(
    val stops: List<BusStopDto>
)
