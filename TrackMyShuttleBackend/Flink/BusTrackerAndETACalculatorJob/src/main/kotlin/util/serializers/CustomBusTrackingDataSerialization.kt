package util.serializers

import kotlinx.serialization.json.Json
import models.BusData
import models.BusTrackingData
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types

class CustomBusTrackingDataDeserializer: DeserializationSchema<BusTrackingData>{

    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }

    override fun deserialize(inputMesage: ByteArray): BusTrackingData {
        return inputMesage.let {
            json.decodeFromString<BusTrackingData>(inputMesage.decodeToString())
        }
    }

    override fun isEndOfStream(nextElement: BusTrackingData): Boolean = false

    override fun getProducedType(): TypeInformation<BusTrackingData> = object : TypeHint<BusTrackingData>(){}.typeInfo
}

class CustomBusTrackingDataSerializer: SerializationSchema<BusTrackingData>{

    @Transient lateinit var json: Json

    override fun open(context: SerializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }

    override fun serialize(message: BusTrackingData): ByteArray {
        return message.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}