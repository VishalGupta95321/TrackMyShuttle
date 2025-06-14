package util.serializers

import kotlinx.serialization.json.Json
import models.BusTrackingData
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types

class CustomBusTrackingDataDeserializer(
    private val json: Json
): DeserializationSchema<BusTrackingData>{
    override fun deserialize(inputMesage: ByteArray?): BusTrackingData? {
        return inputMesage?.let {
            json.decodeFromString<BusTrackingData>(inputMesage.decodeToString())
        }
    }

    override fun isEndOfStream(nextElement: BusTrackingData?): Boolean = false

    override fun getProducedType(): TypeInformation<BusTrackingData?>? = TypeInformation.of(BusTrackingData::class.java)
}

class CustomBusTrackingDataSerializer(
    private val json: Json
): SerializationSchema<BusTrackingData>{
    override fun serialize(message: BusTrackingData?): ByteArray? {
        return message?.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}