package util.serializers

import kotlinx.serialization.json.Json
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types
import models.BusLocationData

class CustomBusLocationSerializer(
    private val json: Json
): DeserializationSchema<BusLocationData>{
    override fun deserialize(inputMesage: ByteArray?): BusLocationData? {
        return inputMesage?.let {
            json.decodeFromString<BusLocationData>(inputMesage.decodeToString())
        }
    }

    override fun isEndOfStream(nextElement: BusLocationData?): Boolean = false

    override fun getProducedType(): TypeInformation<BusLocationData?>? = TypeInformation.of(BusLocationData::class.java)
}

class CustomBusLocationDeserializer(
    private val json: Json
): SerializationSchema<BusLocationData>{
    override fun serialize(message: BusLocationData?): ByteArray? {
        return message?.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}