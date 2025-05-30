package data.respository

import data.model.BasicBusDetails
import data.model.Bus
import data.model.BusStatus
import data.util.BasicBusRepoResult
import data.util.BusEntityAttrUpdate.UpdateStopIds.Companion.StopIdsUpdateAction
import data.util.BusRepoResult

interface BusRepository {
    suspend fun fetchBusByBusId(busId: String): BusRepoResult<Bus>

    suspend fun fetchBusesByIds(busIds: List<String>):BusRepoResult<List<Bus>>

    suspend fun registerBus(bus: Bus): BusRepoResult<String>
    suspend fun updateBusDetails(busId: String,bus: BasicBusDetails): BusRepoResult<BasicBusDetails>
    suspend fun deleteBus(busId: String): BusRepoResult<String>

    suspend fun updateBusStatus(busId: String, status: BusStatus): BasicBusRepoResult

    suspend fun updateCurrentAndNextStop(
        busId: String,
        currentBusStopName: String,
        nextBusStopName: String
    ): BasicBusRepoResult

    suspend fun updateStopIds(busId: String, stopIds: List<String>,  updateAction: StopIdsUpdateAction): BasicBusRepoResult

}

