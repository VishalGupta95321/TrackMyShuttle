package data.entity

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import data.model.Route
import data.util.TableName


private const val TABLE_NAME = "ROUTE_TABLE"

@TableName(TABLE_NAME)
data class RouteEntity(
    @DynamoDbPartitionKey
    val routeId: String,
    val busIds: List<String>?,
    val routeName: String? = null
): DynamoDbModel {
    fun toRoute(): Route {
        return Route(
            routeId = routeId,
            busIds = busIds,
            routeName = routeName
        )
    }
}


object RouteEntityAttributes {
    const val ROUTE_ID = "routeId"
    const val BUS_IDS = "busIds"
    const val ROUTE_NAME = "routeName"
}