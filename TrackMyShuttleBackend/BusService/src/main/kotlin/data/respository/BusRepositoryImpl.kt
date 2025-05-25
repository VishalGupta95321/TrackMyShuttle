package data.respository

import data.entity.BusEntity
import data.exceptions.BusRepoErrors
import data.exceptions.DynamoDbErrors
import data.model.BasicBusDetails
import data.model.Bus
import data.model.BusStatus
import data.model.BusStatus.Companion.fromValue
import data.model.toBusEntity
import data.source.DynamoDbDataSource
import data.util.BasicBusRepoResult
import data.util.BusEntityAttrUpdate
import data.util.BusEntityAttrUpdate.UpdateStopIds.Companion.StopIdsUpdateAction
import data.util.BusRepoResult
import data.util.GetBack
import kotlin.math.abs

class BusRepositoryImpl(
    private val dynamoDbSource: DynamoDbDataSource<BusEntity>
): BusRepository {

    override suspend fun fetchBusByBusId(busId: String): BusRepoResult<Bus> {
        val result = dynamoDbSource.getItem(busId)
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success(result.data?.toBus())
       }
    }

    override suspend fun fetchBusesByIds(busIds: List<String>): BusRepoResult<List<Bus>> {
        val result = dynamoDbSource.getItemsInBatch(busIds)
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success(result.data?.map { it.toBus() })
        }
    }

    override suspend fun registerBus(bus: Bus): BasicBusRepoResult {
        val partitionKey = extractPartitionKeyFromBusId(bus.busId)
            ?: return GetBack.Error(BusRepoErrors.PartitionKeyLimitExceeded)

        val result = dynamoDbSource.putItem(item = bus.toBusEntity(partitionKey.toString()))
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success() /// in the controller return the bus id after successful registration.
        }
    }

    override suspend fun updateBusDetails(
        busId: String,
        bus: BasicBusDetails
    ): BasicBusRepoResult {
        /// FIXME()
        val result = dynamoDbSource.updateItemAttr(
            keyVal = busId,
            update = BusEntityAttrUpdate.UpdateBasicBusDetails(value = bus)
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun deleteBus(busId: String): BasicBusRepoResult {
        val result = dynamoDbSource.deleteItem(busId)
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun updateBusStatus(
        busId: String,
        status: BusStatus
    ): BasicBusRepoResult {
        val result = dynamoDbSource.updateItemAttr(
            update = BusEntityAttrUpdate.UpdateBusStatus(fromValue(status.value)),
            keyVal = busId
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun updateCurrentStop(
        busId: String,
        currentBusStopId: String
    ): BasicBusRepoResult {
        val result = dynamoDbSource.updateItemAttr(
            update = BusEntityAttrUpdate.UpdateCurrentStop(currentBusStopId),  // TODO maybe change with just name or full stop info like name and id
            keyVal = busId
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun updateNextStop(
        busId: String,
        nextBusStopId: String
    ): BasicBusRepoResult {
        val result = dynamoDbSource.updateItemAttr(
            update = BusEntityAttrUpdate.UpdateNextStop(nextBusStopId),
            keyVal = busId
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun updateStopIds(
        busId: String,
        stopIds: List<String>,
        updateAction: StopIdsUpdateAction
    ): BasicBusRepoResult {
        val result = dynamoDbSource.updateItemAttr(
            update = BusEntityAttrUpdate.UpdateStopIds(
                value = stopIds,
                updateAction = updateAction
            ),
            keyVal = busId
        )
        return when (result) {
            is GetBack.Error -> {
                if (result.message is DynamoDbErrors.TypeMismatchForAttribute){
                    println("Came here once")
                    updateStopIds(busId, stopIds, StopIdsUpdateAction.Put)
                } else result.toBusRepoErrors()
            }
            is GetBack.Success -> GetBack.Success()
        }
    }

    private fun GetBack.Error<DynamoDbErrors>.toBusRepoErrors(): GetBack.Error<BusRepoErrors>{
        return when(message) {
            is DynamoDbErrors.ItemDoesNotExists -> GetBack.Error(BusRepoErrors.BusDoesNotExist)
            is DynamoDbErrors.ItemAlreadyExists ->  GetBack.Error(BusRepoErrors.BusAlreadyExists)
            else ->  GetBack.Error(BusRepoErrors.SomethingWentWrong)
        }
    }

    private fun extractPartitionKeyFromBusId(
        busId: String
    ): Int? {
        busId.takeLast(4).let {
           return it.toIntOrNull()?.let {
               if(it <= 1000) abs(it) else null
           }
        }
    }
}