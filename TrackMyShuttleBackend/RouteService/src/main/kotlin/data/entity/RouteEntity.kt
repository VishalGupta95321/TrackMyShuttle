package data.entity

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbSortKey
import data.util.TableName
import org.example.data.model.Coordinate
import org.example.data.model.Route
import kotlin.time.Duration

private const val TABLE_NAME = "ROUTE_TABLE"

@TableName(TABLE_NAME)
data class RouteEntity(
    @DynamoDbPartitionKey
    val routeId: String,   // Stop1+Stop2
    @DynamoDbSortKey
    val routeCount: Int,
    val fromStopId: String,
    val toStopId: String,
    val coordinates: List<Coordinate>,
    val duration: Duration,
    val distanceInMeters: Double,
): DynamoDbModel {
    fun toRoute() = Route(
        routeId = routeId,
        routeCount = routeCount,
        fromStopId = fromStopId,
        toStopId = toStopId,
        coordinates = coordinates,
        duration = duration,
        distanceInMeters = distanceInMeters,
    )
}

object RouteEntityAttributes {
    const val ROUTE_ID = "routeId"
    const val ROUTE_COUNT = "routeCount"
    const val FROM_STOP_ID = "fromStopId"
    const val TO_STOP_ID = "toStopId"
    const val COORDINATES = "coordinates"
    const val DURATION = "duration"
    const val DISTANCE_IN_METERS = "distanceInMeters"
}
