package data.util

import data.entity.BUS_STOP_ADDRESS_INDEX
import data.entity.BusStopEntityAttributes

interface DynamoDbScanRequest

sealed interface BusStopScanRequest : DynamoDbScanRequest {
    data class ScanAddress(
        val substring: String,
        val keyName: String = BusStopEntityAttributes.ADDRESS,
        val indexName: String = BUS_STOP_ADDRESS_INDEX,
    ): BusStopScanRequest
}