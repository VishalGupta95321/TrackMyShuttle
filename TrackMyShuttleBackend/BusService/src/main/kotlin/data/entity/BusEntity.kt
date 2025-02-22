package data.entity

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import data.model.BusStatus

data class BusEntity(
    @DynamoDbPartitionKey
    val busId:String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatus?,
    val stopIds: List<String>,
    val currentStop: String?,
    val nextStop: String?,
): DynamoDbEntity

object BusEntityAttributes {
    const val BUS_ID = "busId"
    const val DRIVER_NAME = "driverName"
    const val ACTIVE_HOURS = "activeHours"
    const val ACTIVE_DAYS = "activeDays"
    const val BUS_STATUS = "busStatus"
    const val STOP_IDS = "stopIds"
    const val CURRENT_STOP = "currentStop"
    const val NEXT_STOP = "nextStop"
}
