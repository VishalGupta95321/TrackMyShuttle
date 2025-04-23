package data.respository

import aws.sdk.kotlin.services.dynamodb.model.Get
import data.entity.BusEntity
import data.model.Bus
import data.model.BusStatus
import data.source.DynamoDbDataSource
import data.util.BasicBusRepoResult
import data.util.BasicDynamoDbResult
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

    override suspend fun registerBus(bus: BusEntity): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun updateBusDetails(
        busId: String,
        busData: BusEntity
    ): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBus(busId: String): BasicBusRepoResult {
        TODO("Not yet implemented")
    }


    override suspend fun updateBusStatus(
        busId: String,
        status: BusStatus
    ): BasicBusRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentAndNextStop(
        busId: String,
        currentBusStopId: String,
        nextBusStopId: String
    ): BasicBusRepoResult {
        TODO("Not yet implemented")
    }


}