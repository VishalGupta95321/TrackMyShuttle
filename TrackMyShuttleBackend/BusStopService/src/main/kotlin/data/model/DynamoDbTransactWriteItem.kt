package data.model

import data.entity.DynamoDbEntity
import data.util.DynamoDbAttrUpdate

data class  DynamoDbTransactWriteItem <T: DynamoDbEntity>(
    val putItem: T?,
    val deleteItemKey: String?,
    //val updateAttribute: Pair<String,DynamoDbAttrUpdate>?
)