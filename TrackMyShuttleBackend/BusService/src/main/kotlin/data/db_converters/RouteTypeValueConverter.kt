package data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.model.BusStatus
import data.util.RouteType
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue


@OptIn(ExperimentalApi::class)
object RouteTypeValueConverter: ValueConverter<RouteType> {

    private val json by inject<Json>(clazz = Json::class.java)

    override fun convertTo(routeType: RouteType): AttributeValue {
        return AttributeValue.S(json.encodeToString(routeType))
    }
    override fun convertFrom(to: AttributeValue): RouteType {
        return json.decodeFromString<RouteType>(to.asS())
    }
}