package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import data.entity.DynamoDbEntity

interface DbItemConverter<T: DynamoDbEntity> {
    fun serializeToAttrValue(obj: T): Map<String, AttributeValue>
    fun deserializeToObject(attrValues: Map<String, AttributeValue>): T
}