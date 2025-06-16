package util.serializers

import kotlinx.serialization.json.Json
import models.BusData
import models.BusStop
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.api.common.typeinfo.TypeInformation

class CustomBusStopDataDeserializer: DeserializationSchema<BusStop> {

    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun deserialize(inputMessgae: ByteArray): BusStop {
        return inputMessgae.let { message ->
            json.decodeFromString<BusStop>(inputMessgae.decodeToString())
        }
    }
    override fun isEndOfStream(p0: BusStop) = false
    override fun getProducedType(): TypeInformation<BusStop> = object : TypeHint<BusStop>(){}.typeInfo
}