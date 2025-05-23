package data.db_converters

import aws.sdk.kotlin.hll.dynamodbmapper.values.ValueConverter
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.smithy.kotlin.runtime.ExperimentalApi
import com.google.gson.Gson
import data.model.Location
import kotlin.jvm.java

@OptIn(ExperimentalApi::class)
val LocationValueConverter = object: ValueConverter<Location> {

    private val gson = Gson()

    override fun convertTo(location: Location): AttributeValue {
        return AttributeValue.S(gson.toJson(location))
    }

    override fun convertFrom(to: AttributeValue): Location {
        val jsonStr = to.asS()
        return gson.fromJson(jsonStr, Location::class.java)
    }

}