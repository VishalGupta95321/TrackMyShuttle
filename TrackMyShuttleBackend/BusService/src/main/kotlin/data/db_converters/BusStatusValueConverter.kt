package data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.model.BusStatus
import data.model.fromValue
import kotlin.text.toInt

@OptIn(ExperimentalApi::class)
val BusStatusValueConverter = object: ValueConverter<BusStatus?> {

    override fun convertTo(busStatus: BusStatus?): AttributeValue {
        if(busStatus == null)
            return AttributeValue.Null(true)

        return AttributeValue.N(busStatus.value.toString())
    }

    override fun convertFrom(to: AttributeValue): BusStatus? {
        if (to.asNOrNull() == null)
            return null
        print("bus stat =  " + to.asN().toInt() + "\n")
        return fromValue(to.asN().toInt())
    }
}