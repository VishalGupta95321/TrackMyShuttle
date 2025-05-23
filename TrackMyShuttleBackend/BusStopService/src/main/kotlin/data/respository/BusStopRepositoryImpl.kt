package data.respository

import data.entity.BusStopEntity
import data.exceptions.BusStopRepoErrors
import data.exceptions.DynamoDbErrors
import data.model.BusStop
import data.model.BusStopScanned
import data.model.DynamoDbTransactWriteItem
import data.model.toBusStopEntity
import data.source.DynamoDbDataSource
import data.util.BasicBusStopRepoResult
import data.util.BusStopRepoResult
import data.util.BusStopScanRequest
import data.util.GetBack

class BusStopRepositoryImpl(
    private val dynamoDbSource: DynamoDbDataSource<BusStopEntity, BusStopScanned>
): BusStopRepository {
    override suspend fun addBusStop(stop: BusStop): BasicBusStopRepoResult {
        val result = dynamoDbSource.putItem(item = stop.toBusStopEntity())
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun addBusStops(stop: List<BusStop>): BasicBusStopRepoResult {
        val result = dynamoDbSource.transactWriteItems( items =
            stop.map { DynamoDbTransactWriteItem(putItem = it.toBusStopEntity(), deleteItemKey = null)}
        )
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun updateBusStop(busStop: BusStop): BasicBusStopRepoResult {
        val result = dynamoDbSource.putItem(item = busStop.toBusStopEntity(), isUpsert = true)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun deleteBusStop(stopId: String): BasicBusStopRepoResult {
        val result = dynamoDbSource.deleteItem(key = stopId)
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }
    override suspend fun deleteBusStops(stopIds: List<String>): BasicBusStopRepoResult {
        val result = dynamoDbSource.transactWriteItems( items =
            stopIds.map { DynamoDbTransactWriteItem(putItem = null, deleteItemKey = it)}
        )
        return when (result) {
            is GetBack.Error -> result.toBusStopRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
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