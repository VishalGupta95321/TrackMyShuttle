package controller

import model.request.BusCurrentNextStopUpdateRequest
import model.request.BusRegistrationOrUpdateRequest
import model.request.BusStatusUpdateRequest
import model.request.BusStopIdsUpdateRequest
import model.response.BasicBusControllerResponse
import model.response.BusResponse
import model.response.BusesResponse
import model.response.BusControllerResponse

interface BusController {

    suspend fun getBus(busId: String): BusControllerResponse<BusResponse>

    suspend fun getBuses(busIds: List<String>): BusControllerResponse<BusesResponse>/// end request with timeout in case fetching takes longer or in a deadlock

    suspend fun registerBus(request: BusRegistrationOrUpdateRequest): BasicBusControllerResponse

    suspend fun updateBusDetails(request: BusRegistrationOrUpdateRequest): BasicBusControllerResponse

    suspend fun deleteBus(busId: String): BasicBusControllerResponse

    suspend fun updateStopIds(request: BusStopIdsUpdateRequest): BasicBusControllerResponse

    suspend fun updateBusStatus(request: BusStatusUpdateRequest): BasicBusControllerResponse // select where id == someId then update busStatus.

    suspend fun updateCurrentAndNextStop(request: BusCurrentNextStopUpdateRequest): BasicBusControllerResponse

}