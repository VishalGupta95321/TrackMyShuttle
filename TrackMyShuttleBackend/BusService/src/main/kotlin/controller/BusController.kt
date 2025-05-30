package controller

import model.request.BusCurrentNextStopUpdateRequest
import model.request.BusRegistrationRequest
import model.request.BusStatusUpdateRequest
import model.request.BusStopIdsUpdateRequest
import model.request.BusUpdateRequest
import model.response.BasicBusControllerResponse
import model.response.BusResponse
import model.response.BusesResponse
import model.response.BusControllerResponse
import model.response.UpdatedBusResponse

interface BusController {

    suspend fun getBus(busId: String): BusControllerResponse<BusResponse>
    suspend fun getBuses(busIds: List<String>): BusControllerResponse<BusesResponse>/// end request with timeout in case fetching takes longer or in a deadlock

    suspend fun registerBus(request: BusRegistrationRequest): BusControllerResponse<String>

    suspend fun updateBusDetails(busId: String, request: BusUpdateRequest): BusControllerResponse<UpdatedBusResponse>

    suspend fun deleteBus(busId: String): BusControllerResponse<String>

    suspend fun updateStopIds(request: BusStopIdsUpdateRequest): BasicBusControllerResponse

    suspend fun updateBusStatus(busId: String, request: BusStatusUpdateRequest): BasicBusControllerResponse

    suspend fun updateCurrentAndNextStop(busId: String, request: BusCurrentNextStopUpdateRequest): BasicBusControllerResponse

}