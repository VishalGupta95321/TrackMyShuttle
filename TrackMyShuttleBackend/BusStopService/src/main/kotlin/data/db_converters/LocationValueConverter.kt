package data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.model.Location
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue
import kotlin.jvm.java

@OptIn(ExperimentalApi::class)
val LocationValueConverter = object: ValueConverter<Location> {

    private val json : Json by inject(
        clazz = Json::class.java
    )

    override fun convertTo(location: Location): AttributeValue {
        return AttributeValue.S(json.encodeToString(location))
    }

    override fun convertFrom(to: AttributeValue): Location {
        val jsonStr = to.asS()
        return json.decodeFromString<Location>(jsonStr)
    }

}