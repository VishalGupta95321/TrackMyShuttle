package data.model

import data.entity.DynamoDbModel

data class  DynamoDbTransactWriteItem <T: DynamoDbModel>(
    val putItem: T?,
    val deleteItemKey: String?,
    //val updateAttribute: Pair<String,DynamoDbAttrUpdate>?
)