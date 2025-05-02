package data.entity

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import data.model.Bus
import data.model.BusStatus
import data.util.TableName

private const val TABLE_NAME = "BUS_TABLE"

@TableName(TABLE_NAME)
data class BusEntity(
    @DynamoDbPartitionKey
    val busId: String,
    val partitionKey: String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatus?,
    val stopIds: List<String>,
    val currentStop: String?,
    val nextStop: String?,
) : DynamoDbEntity {
    fun toBus() = Bus(
        busId = busId,
        driverName = driverName,
        activeHours = activeHours,
        activeDays = activeDays,
        busStatus = busStatus,
        nextStop = nextStop,
        currentStop = currentStop,
    )
}

object BusEntityAttributes {
    const val BUS_ID = "busId"
    const val PARTITION_KEY = "partitionKey"
    const val DRIVER_NAME = "driverName"
    const val ACTIVE_HOURS = "activeHours"
    const val ACTIVE_DAYS = "activeDays"
    const val BUS_STATUS = "busStatus"
    const val STOP_IDS = "stopIds"
    const val CURRENT_STOP = "currentStop"
    const val NEXT_STOP = "nextStop"
}
