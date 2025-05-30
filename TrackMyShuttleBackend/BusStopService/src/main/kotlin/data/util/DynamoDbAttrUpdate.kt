package data.util

import aws.sdk.kotlin.services.dynamodb.model.AttributeAction

interface DynamoDbAttrUpdate


sealed class BusStopEntityAttrUpdate(
    val action: AttributeAction = AttributeAction.Put
) : DynamoDbAttrUpdate {
    data class UpdateBusId(
        val keyVal: String,
        val value: String,
        private val updateAction: BusIdsUpdateAction
    ) : BusStopEntityAttrUpdate(updateAction.action) {
        companion object {
            sealed class BusIdsUpdateAction(val action: AttributeAction) {
                data object Add : BusIdsUpdateAction(AttributeAction.Add)
                data object Delete : BusIdsUpdateAction(AttributeAction.Delete)
            }
        }
    }
}


