package controller

import data.util.BasicBusRepoResult
import model.request.BusCurrentNextStopUpdateRequest
import model.request.BusRegistrationRequest
import model.request.BusStatusUpdateRequest
import model.request.BusUpdateRequest
import model.response.BusControllerResponse
import model.response.BusResponse
import model.response.BusesResponse

class BusControllerImpl: BusController {
    override suspend fun getBus(busId: String): BusControllerResponse<BusResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuses(busIds: List<String>): BusControllerResponse<List<BusesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun registerBus(request: BusRegistrationRequest): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun updateBusDetails(request: BusUpdateRequest): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBus(busId: String): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun updateBusStatus(request: BusStatusUpdateRequest): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentAndNextStop(request: BusCurrentNextStopUpdateRequest): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun getStopIds(busId: String): BusControllerResponse<List<String>> {
        TODO("Not yet implemented")
    }
}

/// return type would be "Http Response" the fields in http response would be the fields required by the handler  TODO