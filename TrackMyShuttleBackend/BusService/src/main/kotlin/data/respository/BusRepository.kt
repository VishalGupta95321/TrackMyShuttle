package data.respository

import data.entity.BusEntity
import data.model.Bus
import data.model.BusStatus
import data.util.BasicBusRepoResult
import data.util.BusRepoResult
import data.util.GetBackBasic

interface BusRepository {
    suspend fun fetchBusByBusId(busId: String): BusRepoResult<Bus>

    // TODO this one is for Route Service
    // suspend fun fetchBusIdsByRouteId(routeId: String): BusRepoResult<List<String>>


    suspend fun fetchBusesByIds(busIds: List<String>):BusRepoResult<List<Bus>> /// end request with timeout in case fetching takes longer or in a deadlock

    suspend fun registerBus(bus: BusEntity): BasicBusRepoResult // will call another lambda function

    suspend fun updateBusDetails(bus: BusEntity): BasicBusRepoResult

    suspend fun deleteBus(busId: String): BasicBusRepoResult

    suspend fun updateBusStatus(busId: String, status: BusStatus): BasicBusRepoResult // select where id == someId then update busStatus.

    suspend fun updateCurrentStop(busId: String, currentBusStopId: String): BasicBusRepoResult

    suspend fun updateNextStop(busId: String,nextBusStopId: String): BasicBusRepoResult

}

