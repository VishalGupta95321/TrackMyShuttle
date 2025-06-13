package util.serializers

import kotlinx.serialization.json.Json
import models.Route
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeinfo.Types


/// FIXME Complete me
class CustomBusRoutesDeserializer(
    private val json: Json
): DeserializationSchema<Route>{
    override fun deserialize(inputMesage: ByteArray?): Route? {
       return inputMesage?.let {
           json.decodeFromString<Route>(it.decodeToString())
       }
    }

    override fun isEndOfStream(nextElement: Route?): Boolean = false

    override fun getProducedType(): TypeInformation<Route?>? = Types.POJO(Route::class.java)

}

class CustomBusRoutesSerializer(
    private val json: Json
): SerializationSchema<Route>{
    override fun serialize(message: Route?): ByteArray? {
        return message?.let {
            json.encodeToString(it).toByteArray()
        }
    }
}

