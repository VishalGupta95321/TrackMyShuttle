package util.serializers

import kotlinx.serialization.json.Json
import models.BusData
import models.BusETAData
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types


class CustomBusETADataDeserializer: DeserializationSchema<BusETAData>{

    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }

    override fun deserialize(inputMesage: ByteArray): BusETAData {
        return inputMesage.let {
            json.decodeFromString<BusETAData>(inputMesage.decodeToString())
        }
    }

    override fun isEndOfStream(nextElement: BusETAData): Boolean = false

    override fun getProducedType(): TypeInformation<BusETAData> = object : TypeHint<BusETAData>(){}.typeInfo
}

class CustomBusETADataSerializer: SerializationSchema<BusETAData>{

    @Transient lateinit var json: Json

    override fun open(context: SerializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }

    override fun serialize(message: BusETAData): ByteArray {
        return message.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}