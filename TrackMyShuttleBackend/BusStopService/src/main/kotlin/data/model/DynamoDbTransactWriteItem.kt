package data.model

import data.entity.DynamoDbModel
import data.util.DynamoDbAttrUpdate

data class  DynamoDbTransactWriteItem <T: DynamoDbModel>(
    val putItem: T?,
    val deleteItemKey: String?,
    val updateItem: TransactionUpdateItem?,
    ){
    companion object{
        data class TransactionUpdateItem(
            val key: String,
            val attrToUpdate: DynamoDbAttrUpdate
        )
    }
}
