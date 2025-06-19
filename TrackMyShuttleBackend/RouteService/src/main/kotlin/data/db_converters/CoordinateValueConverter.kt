package org.example.data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import kotlinx.serialization.json.Json
import org.example.data.model.Coordinate
import org.koin.java.KoinJavaComponent.inject


@OptIn(ExperimentalApi::class)
object CoordinateValueConverter: ValueConverter<List<Coordinate>> {

    private val json by inject<Json>(clazz = Json::class.java)

    override fun convertTo(value: List<Coordinate>): AttributeValue {
        return AttributeValue.Ss(value.map { json.encodeToString(it) })
    }
    override fun convertFrom(to: AttributeValue): List<Coordinate> {
        return to.asSs().map { json.decodeFromString<Coordinate>(it) }
    }
}