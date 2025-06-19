package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.entity.RouteEntity
import data.entity.RouteEntityAttributes
import org.example.data.db_converters.CoordinateValueConverter
import org.example.data.db_converters.DurationValueConverter
import kotlin.time.Duration


class RouteItemConverter() : DbItemConverter<RouteEntity> {

    @OptIn(ExperimentalApi::class)
    override fun serializeToAttrValue(
        obj: RouteEntity
    ): Map<String, AttributeValue> {
        return mapOf(
            RouteEntityAttributes.ROUTE_ID to AttributeValue.S(obj.routeId),
            RouteEntityAttributes.ROUTE_COUNT to AttributeValue.S(obj.routeCount.toString()),
            RouteEntityAttributes.FROM_STOP_ID to AttributeValue.S(obj.fromStopId),
            RouteEntityAttributes.TO_STOP_ID to AttributeValue.S(obj.toStopId),
            RouteEntityAttributes.DURATION to DurationValueConverter.convertTo(obj.duration),
            RouteEntityAttributes.DISTANCE_IN_METERS to AttributeValue.S(obj.distanceInMeters.toString()),
            RouteEntityAttributes.COORDINATES to CoordinateValueConverter.convertTo(obj.coordinates)
        )
    }

    @OptIn(ExperimentalApi::class)
    override fun deserializeToObject(
        attrValues: Map<String, AttributeValue>
    ): RouteEntity {
        return RouteEntity(
            routeId = attrValues[RouteEntityAttributes.ROUTE_ID]?.asS() ?: throw IllegalArgumentException("Missing routeId"),
            routeCount = attrValues[RouteEntityAttributes.ROUTE_COUNT]?.asS()?.toInt() ?: 0,
            fromStopId = attrValues[RouteEntityAttributes.FROM_STOP_ID]?.asS() ?: "",
            toStopId = attrValues[RouteEntityAttributes.TO_STOP_ID]?.asS() ?: "",
            coordinates = attrValues[RouteEntityAttributes.COORDINATES]?.let { CoordinateValueConverter.convertFrom(it) } ?: throw IllegalArgumentException("Missing coordinates"),
            duration = attrValues[RouteEntityAttributes.DURATION]?.let { DurationValueConverter.convertFrom(it) } ?: Duration.ZERO,
            distanceInMeters = attrValues[RouteEntityAttributes.DISTANCE_IN_METERS]?.asS()?.toDouble() ?: 0.0
        )
    }
}