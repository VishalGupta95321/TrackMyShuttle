package controller

import data.model.BusStop
import data.model.BusStopScanned
import data.util.BasicBusStopRepoResult
import data.util.BusStopEntityAttrUpdate.UpdateBusId.Companion.BusIdsUpdateAction
import data.util.BusStopRepoResult
import model.request.AddBusStopRequest
import model.request.BatchAddBusStopRequest
import model.request.UpdateBusIdsInStopsRequest
import model.request.UpdateBusIdsInStopsRequest.Companion.UpdateType
import model.request.UpdateBusStopRequest
import model.response.BasicBusStopControllerResponse
import model.response.BusIdsResponse
import model.response.BusStopControllerResponse
import model.response.BusStopResponse
import model.response.BusStopScannedResponse
import model.response.BusStopsResponse
import model.response.UpdatedBusStopResponse

interface BusStopController {
    suspend fun getBusStops(stopIds: List<String>): BusStopControllerResponse<BusStopsResponse>
    suspend fun getBusStop(stopId: String): BusStopControllerResponse<BusStopResponse>
    suspend fun getBusStopsByAddressSubstring(subString: String): BusStopControllerResponse<BusStopScannedResponse>
    suspend fun deleteBusStop(stopId: String): BusStopControllerResponse<String>
    suspend fun batchDeleteBusStop(stopIds: List<String>): BusStopControllerResponse<List<String>>

    suspend fun addBusStop(stop: AddBusStopRequest): BusStopControllerResponse<String>
    suspend fun batchAddBusStop(stops: BatchAddBusStopRequest): BusStopControllerResponse<List<String>>

    suspend fun updateBusStop(stopId: String, request: UpdateBusStopRequest): BusStopControllerResponse<UpdatedBusStopResponse>

    suspend fun updateBusIdsInStops(
        request: UpdateBusIdsInStopsRequest
    ): BasicBusStopControllerResponse


    suspend fun getBusIdsByStops(fromStop: String, toStop: String): BusStopControllerResponse<BusIdsResponse>
}