package data.respository

import data.model.BusStop
import data.model.BusStopScanned
import data.util.BasicBusStopRepoResult
import data.util.BusStopEntityAttrUpdate.UpdateBusId.Companion.BusIdsUpdateAction
import data.util.BusStopRepoResult

interface BusStopRepository {
    suspend fun addBusStop(stop: BusStop): BusStopRepoResult<String>
    suspend fun addBusStops(stop: List<BusStop>): BusStopRepoResult<List<String>>

    suspend fun updateBusStop(
        busStop: BusStop
    ): BusStopRepoResult<BusStop>

    suspend fun deleteBusStop(stopId: String): BusStopRepoResult<String> /// also delete the routes and remove stop id from the bus // can use transact write
    suspend fun deleteBusStops(stopIds: List<String>): BusStopRepoResult<List<String>>
    suspend fun fetchBusStop(stopId: String): BusStopRepoResult<BusStop>
    suspend fun fetchBusStops(stopId: List<String>): BusStopRepoResult<List<BusStop>>

    suspend fun fetchBusStopsByAddressSubstring(
        substring: String,
    ): BusStopRepoResult<List<BusStopScanned>>

    suspend fun updateBusIdsInStops(
        busId: String,
        stopIds: List<String>,
        updateAction: BusIdsUpdateAction
    ): BasicBusStopRepoResult

    suspend fun fetchBusIdsByStops(
        fromStop: String,
        toStop: String
    ): BusStopRepoResult<List<String>>
}