package data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.model.StopIdsWithWaitTime
import data.util.RouteType
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

private const val PLACEHOLDER_Stop_ID_FOR_BUS = "NONE"


@OptIn(ExperimentalApi::class)
object StopIdsWithWaitTimeValueConverter: ValueConverter<List<StopIdsWithWaitTime>> {

    private val json by inject<Json>(clazz = Json::class.java)


    override fun convertTo(value: List<StopIdsWithWaitTime>): AttributeValue {
        return if (value.isNotEmpty())
            AttributeValue.Ss(value.map { json.encodeToString(it) })
        else AttributeValue.Ss(listOf(PLACEHOLDER_Stop_ID_FOR_BUS))
    }
    override fun convertFrom(to: AttributeValue): List<StopIdsWithWaitTime> {
        return to.asSs().minus(PLACEHOLDER_Stop_ID_FOR_BUS).map { json.decodeFromString<StopIdsWithWaitTime>(it) }
    }
}