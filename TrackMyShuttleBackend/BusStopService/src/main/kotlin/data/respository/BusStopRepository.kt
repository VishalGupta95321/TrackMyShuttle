package data.respository

import data.model.BusStop
import data.model.BusStopScanned
import data.util.BasicBusStopRepoResult
import data.util.BusStopRepoResult

interface BusStopRepository {
    suspend fun addBusStop(stop: BusStop): BasicBusStopRepoResult
    suspend fun addBusStops(stop: List<BusStop>): BasicBusStopRepoResult
    suspend fun updateBusStop(busStop: BusStop): BasicBusStopRepoResult
    suspend fun deleteBusStop(stopId: String): BasicBusStopRepoResult /// also delete the routes and remove stop id from the bus // can use transact write
    suspend fun deleteBusStops(stopIds: List<String>): BasicBusStopRepoResult
    suspend fun fetchBusStop(stopId: String): BusStopRepoResult<BusStop>
    suspend fun fetchBusStops(stopId: List<String>): BusStopRepoResult<List<BusStop>>
    suspend fun fetchBusStopsByAddressSubstring(
        substring: String,
    ): BusStopRepoResult<List<BusStopScanned>>
}