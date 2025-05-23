package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.entity.BusStopEntityAttributes
import data.model.BusStopScanned


class BusStopScanResponseItemConverter : DbItemConverter<BusStopScanned> {

    @OptIn(ExperimentalApi::class)
    override fun serializeToAttrValue(
        obj: BusStopScanned
    ): Map<String, AttributeValue> {
        return mapOf(
            BusStopEntityAttributes.STOP_ID to AttributeValue.S(obj.stopId),
            BusStopEntityAttributes.STOP_NAME to AttributeValue.S(obj.stopName),
            BusStopEntityAttributes.ADDRESS to AttributeValue.S(obj.address),
        )
    }

    @OptIn(ExperimentalApi::class)
    override fun deserializeToObject(
        attrValues: Map<String, AttributeValue>
    ): BusStopScanned {
        return BusStopScanned(
            stopId = attrValues[BusStopEntityAttributes.STOP_ID]?.asS() ?: throw IllegalArgumentException("Missing stopId"),
            stopName = attrValues[BusStopEntityAttributes.STOP_NAME]?.asS() ?: "",
            address = attrValues[BusStopEntityAttributes.ADDRESS]?.asS() ?: "",
        )
    }
}
