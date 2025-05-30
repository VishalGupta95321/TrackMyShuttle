package data.entity

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import data.model.BusStop
import data.model.Location
import data.util.TableName

private const val TABLE_NAME = "BUS_STOP_TABLE"
const val BUS_STOP_ADDRESS_INDEX = "stopName-address-index"

@TableName(TABLE_NAME)
data class BusStopEntity(
    @DynamoDbPartitionKey
    val stopId: String,
    val stopName: String,
    val address: String,
    val location: Location,
    val busIds: List<String> = listOf(),
): DynamoDbModel{
    fun toBusStop() = BusStop(
        stopId = stopId,
        stopName = stopName,
        address = address,
        location = location,
    )
}

object BusStopEntityAttributes {
    const val STOP_ID = "stopId"
    const val STOP_NAME = "stopName"
    const val ADDRESS = "address"
    const val LOCATION = "location"
    const val BUS_IDS = "busIds"
}