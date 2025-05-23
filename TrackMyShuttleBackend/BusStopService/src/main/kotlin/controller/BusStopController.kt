package controller

import data.model.BusStop
import model.request.AddBusStopRequest
import model.request.UpdateBusStopRequest
import model.response.BasicBusStopControllerResponse
import model.response.BusStopControllerResponse
import model.response.BusStopDto
import model.response.BusStopResponse
import model.response.BusStopsResponse

interface BusStopController {

    suspend fun getBusStops(stopIds: List<String>): BusStopControllerResponse<BusStopsResponse>
    suspend fun getBusStop(stopId: String): BusStopControllerResponse<BusStopResponse>

    suspend fun deleteBusStop(stopIds: List<String>): BasicBusStopControllerResponse
    suspend fun addBusStop(stops: AddBusStopRequest): BasicBusStopControllerResponse
    suspend fun updateBusStop(request: UpdateBusStopRequest): BasicBusStopControllerResponse

}