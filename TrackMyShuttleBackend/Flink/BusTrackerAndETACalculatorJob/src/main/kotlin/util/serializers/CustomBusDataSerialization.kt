package util.serializers

import kotlinx.serialization.json.Json
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types
import models.BusData

class CustomBusDataSerializer(
    private val json: Json
) : DeserializationSchema<BusData> {
    override fun deserialize(inputMessgae: ByteArray?): BusData? {
       return inputMessgae?.let { message ->
            json.decodeFromString<BusData>(inputMessgae.decodeToString())
        }
    }

    override fun isEndOfStream(p0: BusData?) = false

    override fun getProducedType(): TypeInformation<BusData?> = Types.POJO(BusData::class.java)
}

class CustomBusDataDeserializer(
    private val json: Json
): SerializationSchema<BusData>{
    override fun serialize(message: BusData?): ByteArray? {
        return message?.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}