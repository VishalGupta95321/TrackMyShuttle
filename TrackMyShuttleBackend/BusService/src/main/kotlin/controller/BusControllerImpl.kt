package controller

import data.exceptions.BusRepoErrors
import data.respository.BusRepository
import data.util.BusEntityAttrUpdate.UpdateStopIds.Companion.StopIdsUpdateAction
import data.util.GetBack
import exceptions.BusControllerExceptions
import model.request.BusCurrentNextStopUpdateRequest
import model.request.BusRegistrationOrUpdateRequest
import model.request.BusStatusUpdateRequest
import model.request.BusStopIdsUpdateRequest
import model.request.BusStopIdsUpdateRequest.Companion.UpdateType
import model.request.toBasicBus
import model.request.toBus
import model.response.BasicBusControllerResponse
import model.response.BusControllerResponse
import model.response.BusDto.Companion.fromBus
import model.response.BusResponse
import model.response.BusesResponse

class BusControllerImpl(
    private val busRepository: BusRepository
): BusController {
    override suspend fun getBus(busId: String): BusControllerResponse<BusResponse> {
        val result = busRepository.fetchBusByBusId(busId)
        return when(result) {
            is GetBack.Success -> BusControllerResponse.Success(
                result.data?.let { BusResponse( fromBus(it)) }
            )
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    override suspend fun getBuses(busIds: List<String>): BusControllerResponse<BusesResponse> {

        if (busIds.isEmpty()) return BusControllerResponse.Error(BusControllerExceptions.InvalidInput)

        val result = busRepository.fetchBusesByIds(busIds)
        return when(result) {
            is GetBack.Success -> {
                val buses = result.data?.map { fromBus(it) }
                BusControllerResponse.Success(
                    buses?.let { BusesResponse(it)}
                )
            }
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    override suspend fun registerBus(request: BusRegistrationOrUpdateRequest): BasicBusControllerResponse {
        val result = busRepository.registerBus(request.toBus())
        return when(result) {
            is GetBack.Success -> BusControllerResponse.Success()
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    override suspend fun updateBusDetails(request: BusRegistrationOrUpdateRequest): BasicBusControllerResponse {
        val result = busRepository.updateBusDetails(
            busId = request.busId,
            bus = request.toBasicBus()
        )
        return when(result) {
            is GetBack.Success -> BusControllerResponse.Success()
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    override suspend fun deleteBus(busId: String): BasicBusControllerResponse {
        val result = busRepository.deleteBus(busId)
        return when(result) {
            is GetBack.Success -> BusControllerResponse.Success()
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    override suspend fun updateStopIds(request: BusStopIdsUpdateRequest): BasicBusControllerResponse {
        if(request.stopIds.isEmpty()) return BusControllerResponse.Error(BusControllerExceptions.InvalidInput)

        val updateAction = when(request.updateType){
           UpdateType.Add -> StopIdsUpdateAction.Add
           UpdateType.Remove -> StopIdsUpdateAction.Delete
        }
        val result = busRepository.updateStopIds(
            busId = request.busId,
            stopIds = request.stopIds,
            updateAction = updateAction
        )
        return when(result) {
            is GetBack.Success -> BusControllerResponse.Success()
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    override suspend fun updateBusStatus(request: BusStatusUpdateRequest): BasicBusControllerResponse {
        val result = busRepository.updateBusStatus(request.busId,request.busStatus.toBusStatus())
        return when(result) {
            is GetBack.Success -> BusControllerResponse.Success()
            is GetBack.Error -> result.toBusControllerErrors()
        }
    }

    /// TODO(" update to use transactions")
    override suspend fun updateCurrentAndNextStop(request: BusCurrentNextStopUpdateRequest): BasicBusControllerResponse {
        val result = busRepository.updateCurrentStop(request.busId,request.currentStopName)
        return when(result) {
            is GetBack.Error -> result.toBusControllerErrors()
            is GetBack.Success -> {
                val result = busRepository.updateNextStop(request.busId,request.nextStopName)
                when(result) {
                    is GetBack.Success -> BusControllerResponse.Success()
                    is GetBack.Error -> result.toBusControllerErrors()
                }
            }
        }
    }


    private fun GetBack.Error<BusRepoErrors>.toBusControllerErrors(): BusControllerResponse.Error{
        return when(message) {
            is BusRepoErrors.BusDoesNotExist -> BusControllerResponse.Error(BusControllerExceptions.ItemNotFound)
            is BusRepoErrors.BusAlreadyExists -> BusControllerResponse.Error(BusControllerExceptions.ItemAlreadyExists)
            is BusRepoErrors.PartitionKeyLimitExceeded -> BusControllerResponse.Error(BusControllerExceptions.RegistrationError)
            else ->  BusControllerResponse.Error(BusControllerExceptions.SomethingWentWrong)
        }
    }
}

/// return type would be "Http Response" the fields in http response would be the fields required by the handler  TODO