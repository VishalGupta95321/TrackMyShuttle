package util.serializers

import kotlinx.serialization.json.Json
import models.BusData
import models.Route
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types

class CustomBusRoutesDeserializer: DeserializationSchema<Route>{

    @Transient lateinit var json: Json

    override fun open(context: DeserializationSchema.InitializationContext?) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun deserialize(inputMesage: ByteArray): Route {
       return inputMesage.let {
           json.decodeFromString<Route>(it.decodeToString())
       }
    }
    override fun isEndOfStream(nextElement: Route): Boolean = false
    override fun getProducedType(): TypeInformation<Route> = object : TypeHint<Route>(){}.typeInfo

}


