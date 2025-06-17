package util

import data.util.RouteType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import model.RouteTypeDto


object CustomRouteTypeDtoSerializer : KSerializer<RouteTypeDto> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("RouteTypeDto", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): RouteTypeDto {
        return when (decoder.decodeString()) {
            "Loop" -> RouteTypeDto.Loop
            "OutAndBack" -> RouteTypeDto.OutAndBack
            else -> throw SerializationException()
        }
    }

    override fun serialize(encoder: Encoder, value: RouteTypeDto) {
        encoder.encodeString(
            when(value){
                RouteTypeDto.Loop -> "Loop"
                RouteTypeDto.OutAndBack -> "OutAndBack"
            }
        )
    }
}