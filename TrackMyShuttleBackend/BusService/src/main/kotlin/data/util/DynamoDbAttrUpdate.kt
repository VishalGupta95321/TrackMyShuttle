package data.util

import aws.sdk.kotlin.services.dynamodb.model.AttributeAction

sealed interface DynamoDbAttrUpdate {
    sealed class BusDataAttrUpdate(
        val action: AttributeAction = AttributeAction.Put
    ): DynamoDbAttrUpdate {
        data class UpdateBusStatus(val value: data.model.BusStatus) : BusDataAttrUpdate()

        data class UpdateStopIds(
            val value: List<String>,
            val updateAction: StopIdsUpdateAction
        ): BusDataAttrUpdate(updateAction.action){
            companion object{
                sealed class StopIdsUpdateAction(val action: AttributeAction){
                    data object Put: StopIdsUpdateAction(AttributeAction.Put)
                    data object Add: StopIdsUpdateAction(AttributeAction.Add)
                    data object Delete: StopIdsUpdateAction(AttributeAction.Delete)
                }
            }
        }
        data class UpdateCurrentStop(val value: String?) : BusDataAttrUpdate()
        data class UpdateNextStop(val value: String?) : BusDataAttrUpdate()
    }
} // update this