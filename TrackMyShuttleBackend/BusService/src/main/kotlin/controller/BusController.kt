package controller

import data.util.BasicBusRepoResult
import model.request.BusCurrentNextStopUpdateRequest
import model.request.BusRegistrationRequest
import model.request.BusStatusUpdateRequest
import model.request.BusUpdateRequest
import model.response.BusResponse
import model.response.BusesResponse
import model.response.BusControllerResponse

interface BusController {

    suspend fun getBus(busId: String): BusControllerResponse<BusResponse>

    suspend fun getBuses(busIds: List<String>): BusControllerResponse<List<BusesResponse>>/// end request with timeout in case fetching takes longer or in a deadlock

    suspend fun registerBus(request: BusRegistrationRequest): BasicBusRepoResult // will call another lambda function

    suspend fun updateBusDetails(request: BusUpdateRequest): BasicBusRepoResult

    suspend fun deleteBus(busId: String): BasicBusRepoResult

    suspend fun updateBusStatus(request: BusStatusUpdateRequest): BasicBusRepoResult // select where id == someId then update busStatus.

    suspend fun updateCurrentAndNextStop(request: BusCurrentNextStopUpdateRequest): BasicBusRepoResult

    suspend fun getStopIds(busId: String): BusControllerResponse<List<String>>

}