package util.serializers

import kotlinx.serialization.json.Json
import models.RawBusStop
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.api.common.typeinfo.TypeInformation

class CustomBusStopDataDeserializer: DeserializationSchema<RawBusStop> {

    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun deserialize(inputMessgae: ByteArray): RawBusStop {
        return inputMessgae.let { message ->
            json.decodeFromString<RawBusStop>(inputMessgae.decodeToString())
        }
    }
    override fun isEndOfStream(p0: RawBusStop) = false
    override fun getProducedType(): TypeInformation<RawBusStop> = object : TypeHint<RawBusStop>(){}.typeInfo
}