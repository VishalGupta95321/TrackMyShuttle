package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi

import data.entity.RouteEntity
import data.entity.RouteEntityAttributes


private const val PLACEHOLDER_BUS_ID_FOR_ROUTE = "NONE"

class RouteItemConverter : DbItemConverter<RouteEntity> {

    @OptIn(ExperimentalApi::class)
    override fun serializeToAttrValue(
        obj: RouteEntity
    ): Map<String, AttributeValue> {
        return mapOf(
            RouteEntityAttributes.ROUTE_ID to AttributeValue.S(obj.routeId),
            RouteEntityAttributes.ROUTE_NAME to if(obj.routeName != null) AttributeValue.S(obj.routeName) else AttributeValue.Null(true),
            RouteEntityAttributes.BUS_IDS to if(obj.busIds != null && obj.busIds.isNotEmpty()) AttributeValue.Ss(obj.busIds) else AttributeValue.Ss(listOf(PLACEHOLDER_BUS_ID_FOR_ROUTE)),
        )
    }

    @OptIn(ExperimentalApi::class)
    override fun deserializeToObject(
        attrValues: Map<String, AttributeValue>
    ): RouteEntity {
        return RouteEntity(
            routeId = attrValues[RouteEntityAttributes.ROUTE_ID]?.asS() ?: throw IllegalArgumentException("Missing stopId"),
            routeName = attrValues[RouteEntityAttributes.ROUTE_NAME]?.asS() ?: "",
            busIds = attrValues[RouteEntityAttributes.BUS_IDS]?.asSsOrNull()?.minus(PLACEHOLDER_BUS_ID_FOR_ROUTE),
        )
    }
}
