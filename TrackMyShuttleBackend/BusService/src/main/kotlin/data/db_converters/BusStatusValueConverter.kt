package data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.model.BusStatus
import data.model.BusStatus.Companion.fromValue
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import kotlin.text.toInt

@OptIn(ExperimentalApi::class)
object BusStatusValueConverter: ValueConverter<BusStatus?> {

    private val json by inject<Json>(clazz = Json::class.java)

    override fun convertTo(busStatus: BusStatus?): AttributeValue {
        if(busStatus == null)
            return AttributeValue.Null(true)

        return AttributeValue.S(json.encodeToString(busStatus))
    }

    override fun convertFrom(to: AttributeValue): BusStatus? {
        if (to.asSOrNull() == null)
            return null
        return json.decodeFromString<BusStatus>(to.asS())
    }
}