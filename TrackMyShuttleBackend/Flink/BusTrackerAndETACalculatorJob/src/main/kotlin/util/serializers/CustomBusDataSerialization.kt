package util.serializers

import kotlinx.serialization.json.Json
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types
import models.BusData
import org.apache.flink.api.common.typeinfo.TypeHint

class CustomBusDataSerializer: DeserializationSchema<BusData> {

    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }

    override fun deserialize(inputMessgae: ByteArray): BusData {
       return inputMessgae.let { message ->
            json.decodeFromString<BusData>(inputMessgae.decodeToString())
        }
    }

    override fun isEndOfStream(p0: BusData?) = false

    override fun getProducedType(): TypeInformation<BusData> = object : TypeHint<BusData>(){}.typeInfo
}

class CustomBusDataDeserializer: SerializationSchema<BusData>{

    @Transient lateinit var json: Json

    override fun open(context: SerializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }

    override fun serialize(message: BusData): ByteArray {
        return message.let { message ->
            json.encodeToString(message).toByteArray()
        }
    }
}