package data.source

import data.entity.DynamoDbEntity
import data.model.DynamoDbTransactWriteItem
import data.util.BasicDynamoDbResult
import data.util.DynamoDbAttrUpdate
import data.util.DynamoDbResult

interface DynamoDbDataSource<T: DynamoDbEntity> {
    suspend fun getItem(key: String): DynamoDbResult<T>
    suspend fun getItemsInBatch(keys: List<String>): DynamoDbResult<List<T>>
    suspend fun transactWriteItems(
        items: List<DynamoDbTransactWriteItem<T>>
    ): BasicDynamoDbResult

    suspend fun putItem(item: T): BasicDynamoDbResult
    suspend fun deleteItem(key: String): BasicDynamoDbResult
    suspend fun updateItemAttr(
        update: DynamoDbAttrUpdate,
        keyVal: String
    ): BasicDynamoDbResult
}


