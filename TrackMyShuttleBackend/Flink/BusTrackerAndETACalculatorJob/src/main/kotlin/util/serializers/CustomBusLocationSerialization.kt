package util.serializers

import kotlinx.serialization.json.Json
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import models.BusLocationData
import org.apache.flink.api.common.typeinfo.TypeHint

class CustomBusLocationDeserializer: DeserializationSchema<BusLocationData>{


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

