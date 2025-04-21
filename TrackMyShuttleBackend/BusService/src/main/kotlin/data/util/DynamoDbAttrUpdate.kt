package data.util

import aws.sdk.kotlin.services.dynamodb.model.AttributeAction

interface DynamoDbAttrUpdate


sealed class BusEntityAttrUpdate(
    val action: AttributeAction = AttributeAction.Put
) : DynamoDbAttrUpdate {
    data class UpdateBusStatus(val value: data.model.BusStatus) : BusEntityAttrUpdate()

    data class UpdateStopIds(
        val value: List<String>,
        val updateAction: StopIdsUpdateAction
    ) : BusEntityAttrUpdate(updateAction.action) {
        companion object {
            sealed class StopIdsUpdateAction(val action: AttributeAction) {
                data object Put : StopIdsUpdateAction(AttributeAction.Put)
                data object Add : StopIdsUpdateAction(AttributeAction.Add)
                data object Delete : StopIdsUpdateAction(AttributeAction.Delete)
            }
        }
    }

    data class UpdateCurrentStop(val value: String?) : BusEntityAttrUpdate()
    data class UpdateNextStop(val value: String?) : BusEntityAttrUpdate()
}


// update this** Done