package util.serializers

import kotlinx.serialization.json.Json
import models.BusETAData
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types


class CustomBusETADataDeserializer(
    private val json: Json
): DeserializationSchema<BusETAData>{
    override fun deserialize(inputMesage: ByteArray?): BusETAData? {
        return inputMesage?.let {
            json.decodeFromString<BusETAData>(inputMesage.decodeToString())
        }
    }

    override fun isEndOfStream(nextElement: BusETAData?): Boolean = false

    override fun getProducedType(): TypeInformation<BusETAData?>? = TypeInformation.of(BusETAData::class.java)
}

class CustomBusETADataSerializer(
    private val json: Json
): SerializationSchema<BusETAData>{
    override fun serialize(message: BusETAData?): ByteArray? {
        return message?.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}