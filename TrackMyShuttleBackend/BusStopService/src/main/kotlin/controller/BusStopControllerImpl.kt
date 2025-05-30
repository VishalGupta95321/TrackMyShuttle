package controller

import data.exceptions.BusStopRepoErrors
import data.respository.BusStopRepository
import data.util.BusStopEntityAttrUpdate.UpdateBusId.Companion.BusIdsUpdateAction
import data.util.GetBack
import exceptions.BusStopControllerExceptions
import model.request.AddBusStopRequest
import model.request.BatchAddBusStopRequest
import model.request.UpdateBusIdsInStopsRequest
import model.request.UpdateBusIdsInStopsRequest.Companion.UpdateType
import model.request.UpdateBusStopRequest
import model.response.BasicBusStopControllerResponse
import model.response.BusIdsResponse
import model.response.BusStopControllerResponse
import model.response.BusStopDto.Companion.fromBusStop
import model.response.BusStopResponse
import model.response.BusStopScannedDto.Companion.fromBusStopScanned
import model.response.BusStopScannedResponse
import model.response.BusStopsResponse
import model.response.UpdatedBusStopResponse

class BusStopControllerImpl(
    private val busStopRepository: BusStopRepository
): BusStopController {

    override suspend fun getBusStops(stopIds: List<String>): BusStopControllerResponse<BusStopsResponse> {

        validateList(stopIds)?.let { return it }

        val result = busStopRepository.fetchBusStops(stopIds)
        return when(result) {
            is GetBack.Success -> {
                val buses = result.data?.map { fromBusStop(it) }
                BusStopControllerResponse.Success(
                    buses?.let { BusStopsResponse(it)}
                )
            }
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun getBusStop(stopId: String): BusStopControllerResponse<BusStopResponse> {
        val result = busStopRepository.fetchBusStop(stopId)
        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(
                result.data?.let { BusStopResponse( fromBusStop(it)) }
            )
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun getBusStopsByAddressSubstring(
        subString: String
    ): BusStopControllerResponse<BusStopScannedResponse> {
       if(subString.isBlank()) return BusStopControllerResponse.Error(BusStopControllerExceptions.InvalidInput)

        val result = busStopRepository.fetchBusStopsByAddressSubstring(subString)
        return when(result) {
            is GetBack.Success -> {
                val buses = result.data?.map { fromBusStopScanned(it) }
                BusStopControllerResponse.Success(
                    BusStopScannedResponse(buses ?: listOf())
                )
            }
            is GetBack.Error -> result.toBusStopControllerErrors()
        }

    }

    override suspend fun deleteBusStop(stopId: String): BusStopControllerResponse<String> {

        val result = busStopRepository.deleteBusStop(stopId)
        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(result.data)
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun batchDeleteBusStop(stopIds: List<String>): BusStopControllerResponse<List<String>> {
        validateList(stopIds)?.let { return it }

        val result = busStopRepository.deleteBusStops(stopIds)

        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(result.data)
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun addBusStop(request: AddBusStopRequest): BusStopControllerResponse<String> {

        val result = busStopRepository.addBusStop(request.toBusStop())

        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(result.data)
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun batchAddBusStop(request: BatchAddBusStopRequest): BusStopControllerResponse<List<String>> {
        validateList(request.busStops)?.let { return it }

        val result = busStopRepository.addBusStops(request.busStops.map { it.toBusStop() })

        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(result.data)
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun updateBusStop(stopId: String, request: UpdateBusStopRequest): BusStopControllerResponse<UpdatedBusStopResponse> {

        val result = busStopRepository.updateBusStop(request.toBusStop(stopId))
        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(UpdatedBusStopResponse(
                stopName = request.stopName,
                address = request.address,
                location = request.location,
            ))
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun updateBusIdsInStops(
        request: UpdateBusIdsInStopsRequest
    ): BasicBusStopControllerResponse {

        validateList(request.stopIds)?.let { return it }

        val updateAction = when(request.updateType){
            UpdateType.Add -> BusIdsUpdateAction.Add
            UpdateType.Remove  -> BusIdsUpdateAction.Delete
        }
        val result = busStopRepository.updateBusIdsInStops(
            request.busId,
            request.stopIds,
            updateAction
        )
        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success()
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun getBusIdsByStops(
        fromStop: String,
        toStop: String
    ): BusStopControllerResponse<BusIdsResponse> {
        val result = busStopRepository.fetchBusIdsByStops(fromStop, toStop)
        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success(BusIdsResponse(result.data ?: listOf()))
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    private fun validateList(list: List<Any>): BusStopControllerResponse.Error?{
        return if (list.isEmpty())
            BusStopControllerResponse.Error(BusStopControllerExceptions.InvalidInput)
        else null
    }

    private fun GetBack.Error<BusStopRepoErrors>.toBusStopControllerErrors(): BusStopControllerResponse.Error{
        return when(message) {
            is BusStopRepoErrors.BusStopDoesNotExist -> BusStopControllerResponse.Error(BusStopControllerExceptions.ItemNotFound)
            is BusStopRepoErrors.BusStopAlreadyExists -> BusStopControllerResponse.Error(BusStopControllerExceptions.ItemAlreadyExists)
            else ->  BusStopControllerResponse.Error(BusStopControllerExceptions.SomethingWentWrong)
        }
    }
}