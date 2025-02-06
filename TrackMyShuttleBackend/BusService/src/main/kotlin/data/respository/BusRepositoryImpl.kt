package data.respository

import data.entity.BusEntity
import data.model.Bus
import data.model.BusStatus
import data.source.DynamoDbDataSource
import data.util.BusRepoResult
import data.util.GetBackBasic

class BusRepositoryImpl(
    private val networkDataSource: DynamoDbDataSource<BusEntity>
): BusRepository {
    override suspend fun fetchBusByBusId(busId: String): BusRepoResult<Bus> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchBusIdsByRouteId(routeId: String): BusRepoResult<List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchBusesByIds(busIds: List<String>): BusRepoResult<List<Bus>> {
        TODO("Not yet implemented")
    }

    override suspend fun registerBus(bus: BusEntity): GetBackBasic {
        TODO("Not yet implemented")
    }

    override suspend fun updateBusDetails(
        busId: String,
        busData: BusEntity
    ): GetBackBasic {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBus(): GetBackBasic {
        TODO("Not yet implemented")
    }

    override suspend fun updateBusStatus(busId: String, status: BusStatus): GetBackBasic {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentAndNextStop(
        currentBusStopId: String,
        nextBusStopId: String
    ): GetBackBasic {
        TODO("Not yet implemented")
    }


}