package data.util

import aws.sdk.kotlin.services.dynamodb.model.AttributeAction

interface DynamoDbAttrUpdate


sealed class BusStopEntityAttrUpdate(
    val action: AttributeAction = AttributeAction.Put
) : DynamoDbAttrUpdate {
}


