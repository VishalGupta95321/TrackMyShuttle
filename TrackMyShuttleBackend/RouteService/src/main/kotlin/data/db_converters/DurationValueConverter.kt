package org.example.data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.Duration


@OptIn(ExperimentalApi::class)
object DurationValueConverter: ValueConverter<Duration> {

    private val json by inject<Json>(clazz = Json::class.java)

    override fun convertTo(value:Duration): AttributeValue {
        return AttributeValue.S(json.encodeToString(value))
    }
    override fun convertFrom(to: AttributeValue): Duration {
        return json.decodeFromString<Duration>(to.asS())
    }
}