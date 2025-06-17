package data.respository

import data.entity.BusEntity
import data.exceptions.BusRepoErrors
import data.exceptions.DynamoDbErrors
import data.model.BasicBusDetails
import data.model.Bus
import data.model.BusStatus
import data.model.BusStatus.Companion.fromValue
import data.model.DynamoDbTransactWriteItem
import data.model.DynamoDbTransactWriteItem.Companion.TransactionUpdateItem
import data.model.StopIdsWithWaitTime
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

    override suspend fun registerBus(bus: Bus): BusRepoResult<String> {

        val result = dynamoDbSource.putItem(item = bus.toBusEntity())
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success(bus.busId) /// in the controller return the bus id after successful registration.
        }
    }

    override suspend fun updateBusDetails(
        busId: String,
        bus: BasicBusDetails
    ): BusRepoResult<BasicBusDetails> {
        val result = dynamoDbSource.updateItemAttr(
            keyVal = busId,
            update = BusEntityAttrUpdate.UpdateBasicBusDetails(value = bus)
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success(bus)
        }
    }

    override suspend fun deleteBus(busId: String): BusRepoResult<String> {
        val result = dynamoDbSource.deleteItem(busId)
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success(busId)
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

    override suspend fun updateCurrentAndNextStop(
        busId: String,
        currentBusStopName: String,
        nextBusStopName: String
    ): BasicBusRepoResult {

        val currentStopItem = DynamoDbTransactWriteItem<BusEntity>(
            putItem = null,
            deleteItemKey = null,
            updateItem = TransactionUpdateItem(
                key = busId,
                attrToUpdate = BusEntityAttrUpdate.UpdateCurrentStop(
                    value = currentBusStopName,
                )
            )
        )

        val nextStopItem = DynamoDbTransactWriteItem<BusEntity>(
            putItem = null,
            deleteItemKey = null,
            updateItem = TransactionUpdateItem(
                key = busId,
                attrToUpdate = BusEntityAttrUpdate.UpdateNextStop(
                    value = nextBusStopName,
                )
            )
        )

        // TODO maybe change with just name or full stop info like name and id
        val result = dynamoDbSource.transactWriteItems(
            items = listOf(currentStopItem, nextStopItem)
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun updateStopIds(
        busId: String,
        stopIds:List<StopIdsWithWaitTime>,
        updateAction: StopIdsUpdateAction
    ): BasicBusRepoResult {
        val result = dynamoDbSource.updateItemAttr(
            update = BusEntityAttrUpdate.UpdateStopIds(
                value = stopIds,
                updateAction = updateAction,
                keyVal = busId
            ),
            keyVal = busId
        )
        return when (result) {
            is GetBack.Error -> result.toBusRepoErrors()
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
}