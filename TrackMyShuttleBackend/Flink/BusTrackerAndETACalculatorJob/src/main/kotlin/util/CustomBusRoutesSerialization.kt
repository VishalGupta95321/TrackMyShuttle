package util

import kotlinx.serialization.json.Json
import models.Route
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation



/// FIXME Complete me
class CustomBusRoutesSerialization(
    private val json: Json
): DeserializationSchema<Route>{
    override fun deserialize(p0: ByteArray?): Route? {
        TODO("Not yet implemented")
    }

    override fun isEndOfStream(p0: Route?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getProducedType(): TypeInformation<Route?>? {
        TODO("Not yet implemented")
    }
}