package util

import kotlinx.serialization.json.Json
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types
import models.BusStopsData

class CustomBusStopsSerializer(
    private val json: Json
) : DeserializationSchema<BusStopsData> {
    override fun deserialize(inputMessgae: ByteArray?): BusStopsData? {
       return inputMessgae?.let { message ->
            json.decodeFromString<BusStopsData>(inputMessgae.decodeToString())
        }
    }

    override fun isEndOfStream(p0: BusStopsData?) = false

    override fun getProducedType(): TypeInformation<BusStopsData?> = Types.POJO(BusStopsData::class.java)
}

class CustomBusStopsDeserializer(
    private val json: Json
): SerializationSchema<BusStopsData>{
    override fun serialize(message: BusStopsData?): ByteArray? {
        return message?.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}