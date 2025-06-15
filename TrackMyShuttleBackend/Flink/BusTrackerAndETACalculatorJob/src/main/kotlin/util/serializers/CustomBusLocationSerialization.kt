package util.serializers

import kotlinx.serialization.json.Json
import models.BusData
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types
import models.BusLocationData
import org.apache.flink.api.common.typeinfo.TypeHint

class CustomBusLocationSerializer: DeserializationSchema<BusLocationData>{


    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun deserialize(inputMesage: ByteArray): BusLocationData? {
        return inputMesage.let {
            json.decodeFromString<BusLocationData>(inputMesage.decodeToString())
        }
    }
    override fun isEndOfStream(nextElement: BusLocationData?): Boolean = false
    override fun getProducedType(): TypeInformation<BusLocationData> = object : TypeHint<BusLocationData>(){}.typeInfo
}

class CustomBusLocationDeserializer: SerializationSchema<BusLocationData>{

    @Transient lateinit var json: Json

    override fun open(context: SerializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun serialize(message: BusLocationData): ByteArray {
        return message.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}