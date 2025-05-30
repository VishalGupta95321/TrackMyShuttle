package data.respository

import data.entity.BusStopEntity
import data.exceptions.BusStopRepoErrors
import data.exceptions.DynamoDbErrors
import data.model.BusStop
import data.model.BusStopScanned
import data.model.DynamoDbTransactWriteItem
import data.model.DynamoDbTransactWriteItem.Companion.TransactionUpdateItem
import data.model.toBusStopEntity
import data.source.DynamoDbDataSource
import data.util.BasicBusStopRepoResult
import data.util.BusStopEntityAttrUpdate
import data.util.BusStopEntityAttrUpdate.UpdateBusId.Companion.BusIdsUpdateAction
import data.util.BusStopRepoResult
import data.util.BusStopScanRequest
import data.util.GetBack
import kotlinx.coroutines.runBlocking

class BusStopRepositoryImpl(
    private val dynamoDbSource: DynamoDbDataSource<BusStopEntity, BusStopScanned>
): BusStopRepository {

    override suspend fun addBusStop(stop: BusStop):  BusStopRepoResult<String> {
        val result = dynamoDbSource.putItem(item = stop.toBusStopEntity())
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(stop.stopId)
        }
    }

    override suspend fun addBusStops(stop: List<BusStop>): BusStopRepoResult<List<String>> {
        val result = dynamoDbSource.transactWriteItems( items =
            stop.map { DynamoDbTransactWriteItem(putItem = it.toBusStopEntity(), deleteItemKey = null, updateItem = null)}
        )
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(stop.map { it.stopId })
        }
    }


    /// Note: Using putItem here will replace all the fields, if you want any filed unchanged the use UpdateItemAttr( see example in BusService.)
    override suspend fun updateBusStop(busStop: BusStop): BusStopRepoResult<BusStop> {
        val result = dynamoDbSource.putItem(item = busStop.toBusStopEntity(), isUpsert = true)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(busStop)
        }
    }

    override suspend fun deleteBusStop(stopId: String): BusStopRepoResult<String> {
        val result = dynamoDbSource.deleteItem(key = stopId)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(stopId)
        }
    }
    override suspend fun deleteBusStops(stopIds: List<String>): BusStopRepoResult<List<String>> {
        val result = dynamoDbSource.transactWriteItems( items =
            stopIds.map { DynamoDbTransactWriteItem(putItem = null, deleteItemKey = it , null)}
        )
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }


    override suspend fun updateBusIdsInStops(
        busId: String,
        stopIds: List<String>,
        updateAction: BusIdsUpdateAction
    ): BasicBusStopRepoResult {
        val writeItems = stopIds.map { stopId ->
            DynamoDbTransactWriteItem<BusStopEntity>(
                putItem = null,
                deleteItemKey = null,
                updateItem = TransactionUpdateItem(
                    key = stopId,
                    attrToUpdate = BusStopEntityAttrUpdate.UpdateBusId(
                        keyVal = stopId,
                        value = busId,
                        updateAction = updateAction
                    )
                )
            )
        }
        val result = dynamoDbSource.transactWriteItems(items = writeItems)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun fetchBusIdsByStops(
        fromStop: String,
        toStop: String
    ): BusStopRepoResult<List<String>> {

        val fromStopBusIds = mutableSetOf<String>()
        val toStopBusIds =  mutableSetOf<String>()

        dynamoDbSource.getItem(fromStop).let { result ->
            when (result) {
                is GetBack.Error ->  result.toBusStopRepoErrors()
                is GetBack.Success -> result.data?.let {
                    fromStopBusIds.addAll(it.busIds)
                }
            }
        }

        dynamoDbSource.getItem(toStop).let { result ->
            when (result) {
                is GetBack.Error -> result.toBusStopRepoErrors()
                is GetBack.Success -> result.data?.let {
                    toStopBusIds.addAll(it.busIds)
                }
            }
        }
        return GetBack.Success(fromStopBusIds.intersect(toStopBusIds).toList())
    }

    override suspend fun fetchBusStop(stopId: String): BusStopRepoResult<BusStop> {
        val result = dynamoDbSource.getItem(stopId)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(result.data?.toBusStop())
        }
    }

    override suspend fun fetchBusStops(stopId: List<String>): BusStopRepoResult<List<BusStop>> {
        val result = dynamoDbSource.getItemsInBatch(stopId)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(result.data?.map { it.toBusStop() })
        }
    }

    override suspend fun fetchBusStopsByAddressSubstring(
        substring: String,
    ): BusStopRepoResult<List<BusStopScanned>> {
        val result = dynamoDbSource.scanItemsBySubstring (
            BusStopScanRequest.ScanAddress(substring)
        )
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success(result.data)
        }
    }

    private fun GetBack.Error<DynamoDbErrors>.toBusStopRepoErrors(): GetBack.Error<BusStopRepoErrors>{
        return when(message) {
            is DynamoDbErrors.ItemDoesNotExists -> GetBack.Error(BusStopRepoErrors.BusStopDoesNotExist)
            is DynamoDbErrors.ItemAlreadyExists ->  GetBack.Error(BusStopRepoErrors.BusStopAlreadyExists)
            else ->  GetBack.Error(BusStopRepoErrors.SomethingWentWrong)
        }
    }

}