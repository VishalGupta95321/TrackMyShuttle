package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.entity.BusStopEntity
import data.entity.BusStopEntityAttributes
import data.model.Location

private const val PLACEHOLDER_BUS_ID_FOR_BUS_STOP = "NONE"


class BusStopItemConverter : DbItemConverter<BusStopEntity> {

    @OptIn(ExperimentalApi::class)
    override fun serializeToAttrValue(
        obj: BusStopEntity
    ): Map<String, AttributeValue> {
        return mapOf(
            BusStopEntityAttributes.STOP_ID to AttributeValue.S(obj.stopId),
            BusStopEntityAttributes.STOP_NAME to AttributeValue.S(obj.stopName),
            BusStopEntityAttributes.ADDRESS to AttributeValue.S(obj.address),
            BusStopEntityAttributes.LOCATION to LocationValueConverter.convertTo(obj.location),
            BusStopEntityAttributes.BUS_IDS to if(obj.busIds.isNotEmpty()) AttributeValue.Ss(obj.busIds) else AttributeValue.Ss(listOf(PLACEHOLDER_BUS_ID_FOR_BUS_STOP)),
            )
    }

    @OptIn(ExperimentalApi::class)
    override fun deserializeToObject(
        attrValues: Map<String, AttributeValue>
    ): BusStopEntity {
        return BusStopEntity(
            stopId = attrValues[BusStopEntityAttributes.STOP_ID]?.asS() ?: throw IllegalArgumentException("Missing stopId"),
            stopName = attrValues[BusStopEntityAttributes.STOP_NAME]?.asS() ?: "",
            address = attrValues[BusStopEntityAttributes.ADDRESS]?.asS() ?: "",
            location = attrValues[BusStopEntityAttributes.LOCATION]?.let { LocationValueConverter.convertFrom(it)} ?: throw IllegalArgumentException("Missing location"),
            busIds = attrValues[BusStopEntityAttributes.BUS_IDS]?.asSsOrNull()?.minus(PLACEHOLDER_BUS_ID_FOR_BUS_STOP)  ?: listOf(),

        )
    }
}
