package controller

import data.exceptions.BusStopRepoErrors
import data.respository.BusStopRepository
import data.util.GetBack
import exceptions.BusStopControllerExceptions
import model.request.AddBusStopRequest
import model.request.GetBusStopsByAddressSubstringRequest
import model.request.UpdateBusStopRequest
import model.response.BasicBusStopControllerResponse
import model.response.BusStopControllerResponse
import model.response.BusStopDto.Companion.fromBusStop
import model.response.BusStopResponse
import model.response.BusStopScannedDto
import model.response.BusStopScannedDto.Companion.fromBusStopScanned
import model.response.BusStopScannedResponse
import model.response.BusStopsResponse

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
        request: GetBusStopsByAddressSubstringRequest
    ): BusStopControllerResponse<BusStopScannedResponse> {
       if(request.subString.isBlank()) return BusStopControllerResponse.Error(BusStopControllerExceptions.InvalidInput)

        val result = busStopRepository.fetchBusStopsByAddressSubstring(request.subString)
        return when(result) {
            is GetBack.Success -> {
                val buses = result.data?.map { fromBusStopScanned(it) }
                BusStopControllerResponse.Success(
                    BusStopScannedResponse(buses)
                )
            }
            is GetBack.Error -> result.toBusStopControllerErrors()
        }

    }

    override suspend fun deleteBusStop(stopIds: List<String>): BasicBusStopControllerResponse {

        validateList(stopIds)?.let { return it }

        val isCountOne: Boolean = stopIds.count() == 1

        val result = if(isCountOne)
            busStopRepository.deleteBusStop(stopIds.first())
        else
            busStopRepository.deleteBusStops(stopIds)

        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success()
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun addBusStop(request: AddBusStopRequest): BasicBusStopControllerResponse {
        validateList(request.stops)?.let { return it }

        val isCountOne: Boolean = request.stops.count() == 1

        val result = if(isCountOne)
            busStopRepository.addBusStop(request.stops.first().toBusStop())
        else
            busStopRepository.addBusStops(request.stops.map { it.toBusStop() })

        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success()
            is GetBack.Error -> result.toBusStopControllerErrors()
        }
    }

    override suspend fun updateBusStop(request: UpdateBusStopRequest): BasicBusStopControllerResponse {
        val result = busStopRepository.updateBusStop(request.stop.toBusStop())
        return when(result) {
            is GetBack.Success -> BusStopControllerResponse.Success()
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