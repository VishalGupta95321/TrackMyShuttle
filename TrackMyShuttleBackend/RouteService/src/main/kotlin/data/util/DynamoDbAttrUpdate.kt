package data.util

import aws.sdk.kotlin.services.dynamodb.model.AttributeAction
import data.entity.RouteEntityAttributes

interface DynamoDbAttrUpdate


sealed class RouteEntityAttrUpdate(
    val action: AttributeAction = AttributeAction.Put,
    val attrName: String,
) : DynamoDbAttrUpdate {
    data class UpdateBusIds(
        val keyVal: String,
        val value: List<String>,
        private val updateAction: BusIdsUpdateAction
    ) : RouteEntityAttrUpdate(updateAction.action, attrName = RouteEntityAttributes.BUS_IDS) {
        companion object {
            sealed class BusIdsUpdateAction(val action: AttributeAction) {
                data object Put : BusIdsUpdateAction(AttributeAction.Put)
                data object Add : BusIdsUpdateAction(AttributeAction.Add)
                data object Delete : BusIdsUpdateAction(AttributeAction.Delete)
            }
        }
    }
}


sealed class BusEntityAttrUpdate(
    val attrName: String,
    val action: AttributeAction = AttributeAction.Put
) : DynamoDbAttrUpdate {
    data class UpdateStopIds(
        val keyVal: String,
        val value: List<String>,
        private val updateAction: StopIdsUpdateAction
    ) : BusEntityAttrUpdate(action = updateAction.action, attrName = BUS_TABLE_STOP_IDS_ATTRIBUTE_NAME) {

        companion object {

            private const val BUS_TABLE_STOP_IDS_ATTRIBUTE_NAME = "stopIds"

            sealed class StopIdsUpdateAction(val action: AttributeAction) {
                data object Put : StopIdsUpdateAction(AttributeAction.Put)
                data object Add : StopIdsUpdateAction(AttributeAction.Add)
                data object Delete : StopIdsUpdateAction(AttributeAction.Delete)
            }
        }
    }
}

