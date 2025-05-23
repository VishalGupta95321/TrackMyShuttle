package data.model

import data.entity.DynamoDbModel

data class BusStopScanned(
    val stopId: String,
    val stopName: String,
    val address: String,
):DynamoDbModel

object BusStopScanResponseAttributes{
    const val STOP_ID = "stopId"
    const val STOP_NAME = "stopName"
    const val ADDRESS = "address"
}