package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import data.entity.DynamoDbModel

interface DbItemConverter<T: DynamoDbModel> {
    fun serializeToAttrValue(obj: T): Map<String, AttributeValue>
    fun deserializeToObject(attrValues: Map<String, AttributeValue>): T
}