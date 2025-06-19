package data.util

import aws.sdk.kotlin.services.dynamodb.model.AttributeAction

interface DynamoDbAttrUpdate


sealed class RouteEntityAttrUpdate(
     val action: AttributeAction = AttributeAction.Put
) : DynamoDbAttrUpdate {
    // Updates
}


