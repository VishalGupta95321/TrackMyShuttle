package util.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import util.RouteType

object CustomRouteTypeSerializer : KSerializer<RouteType> {
    override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("RouteType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): RouteType {
        return when (decoder.decodeString()) {
            "Loop" -> RouteType.Loop
            "OutAndBack" -> RouteType.OutAndBack
            else -> throw SerializationException()
        }
    }

    override fun serialize(encoder: Encoder, value: RouteType) {
        encoder.encodeString(
            when(value){
                RouteType.Loop -> "Loop"
                RouteType.OutAndBack -> "OutAndBack"
            }
        )
    }
}