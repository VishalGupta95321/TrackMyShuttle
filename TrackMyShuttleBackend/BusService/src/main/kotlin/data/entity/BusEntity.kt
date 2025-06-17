package data.entity

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import data.model.Bus
import data.model.BusStatus
import data.model.StopIdsWithWaitTime
import data.util.RouteType
import data.util.TableName
import kotlinx.serialization.Serializable

private const val TABLE_NAME = "BUS_TABLE"

@TableName(TABLE_NAME)
data class BusEntity(
    @DynamoDbPartitionKey
    val busId: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val stopIds: List<StopIdsWithWaitTime> = listOf(), /// Edit
    val routeType: RouteType,
    val busStatus: BusStatus?,
    val currentStop: String?,
    val nextStop: String?,
) : DynamoDbModel {
    fun toBus() = Bus(
        busId = busId,
        driverName = driverName,
        activeHours = activeHours,
        activeDays = activeDays,
        routeType = routeType,
        busStatus = busStatus,
        nextStop = nextStop,
        currentStop = currentStop,
        stopIds = stopIds,
    )
}

object BusEntityAttributes {
    const val BUS_ID = "busId"
    const val DRIVER_NAME = "driverName"
    const val ACTIVE_HOURS = "activeHours"
    const val ACTIVE_DAYS = "activeDays"
    const val BUS_STATUS = "busStatus"
    const val STOP_IDS = "stopIds"
    const val CURRENT_STOP = "currentStop"
    const val NEXT_STOP = "nextStop"
    const val ROUTE_TYPE = "routeType"
}
