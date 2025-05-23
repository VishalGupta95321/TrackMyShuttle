package data.source

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import data.entity.DynamoDbModel
import data.model.DynamoDbTransactWriteItem
import data.util.BasicDynamoDbResult
import data.util.DynamoDbAttrUpdate
import data.util.DynamoDbResult
import data.util.DynamoDbScanRequest

interface DynamoDbDataSource<T: DynamoDbModel, F: DynamoDbModel> {
    suspend fun getItem(key: String): DynamoDbResult<T>
    suspend fun getItemsInBatch(keys: List<String>): DynamoDbResult<List<T>>
    suspend fun transactWriteItems(
        items: List<DynamoDbTransactWriteItem<T>>
    ): BasicDynamoDbResult

    suspend fun putItem(
        item: T,
        isUpsert: Boolean = false,
    ): BasicDynamoDbResult
    suspend fun deleteItem(key: String): BasicDynamoDbResult
    suspend fun updateItemAttr(
        update: DynamoDbAttrUpdate,
        keyVal: String
    ): BasicDynamoDbResult

    suspend fun scanItemsBySubstring(request: DynamoDbScanRequest): DynamoDbResult<List<F>?>
}


