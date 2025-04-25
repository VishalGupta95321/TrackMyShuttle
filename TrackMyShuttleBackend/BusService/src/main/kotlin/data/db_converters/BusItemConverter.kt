package data.db_converters

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.entity.BusEntity
import data.entity.BusEntityAttributes


class BusItemConverter : DbItemConverter<BusEntity> {

    @OptIn(ExperimentalApi::class)
    override fun serializeToAttrValue(
        obj: BusEntity
    ): Map<String, AttributeValue> {
        return mapOf(
            BusEntityAttributes.BUS_ID to AttributeValue.S(obj.busId),
            BusEntityAttributes.DRIVER_NAME to AttributeValue.S(obj.driverName),
            BusEntityAttributes.ACTIVE_HOURS to AttributeValue.S(obj.activeHours),
            BusEntityAttributes.ACTIVE_DAYS to AttributeValue.S(obj.activeDays),
            BusEntityAttributes.BUS_STATUS to BusStatusValueConverter.convertTo(obj.busStatus),
            BusEntityAttributes.STOP_IDS to AttributeValue.Ss(obj.stopIds),
            BusEntityAttributes.CURRENT_STOP to if(obj.currentStop!=null) AttributeValue.S(obj.currentStop) else AttributeValue.Null(true),
            BusEntityAttributes.NEXT_STOP to if(obj.nextStop!=null) AttributeValue.S(obj.nextStop) else AttributeValue.Null(true),
        )
    }

    @OptIn(ExperimentalApi::class)
    override fun deserializeToObject(
        attrValues: Map<String, AttributeValue>
    ): BusEntity {
        return BusEntity(
            busId = attrValues[BusEntityAttributes.BUS_ID]?.asS() ?: throw IllegalArgumentException("Missing busId"),
            driverName = attrValues[BusEntityAttributes.DRIVER_NAME]?.asS() ?: "",
            activeHours = attrValues[BusEntityAttributes.ACTIVE_HOURS]?.asS() ?: "",
            activeDays = attrValues[BusEntityAttributes.ACTIVE_DAYS]?.asS() ?: "",
            busStatus = attrValues[BusEntityAttributes.BUS_STATUS]?.let { BusStatusValueConverter.convertFrom(it) },
            stopIds = attrValues[BusEntityAttributes.STOP_IDS]?.asSs() ?: listOf(),
            currentStop = attrValues[BusEntityAttributes.CURRENT_STOP]?.asSOrNull(),
            nextStop =  attrValues[BusEntityAttributes.NEXT_STOP]?.asSOrNull(),
        )
    }
}
