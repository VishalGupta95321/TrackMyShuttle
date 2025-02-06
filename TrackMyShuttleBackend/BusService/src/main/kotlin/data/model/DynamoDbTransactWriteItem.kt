package data.model

import data.entity.DynamoDbEntity

data class  DynamoDbTransactWriteItem <T: DynamoDbEntity>(
    val putItem: T?,
    val deleteItemKey: String?
)