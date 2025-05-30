package util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import model.request.BusStopIdsUpdateRequest.Companion.UpdateType

object CustomUpdateTypeSerializer : KSerializer<UpdateType> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UpdateType", PrimitiveKind.STRING)

    @Throws(SerializationException::class)
    override fun serialize(
        encoder: Encoder,
        value: UpdateType
    ) {
        encoder.encodeString(
            when(value) {
                UpdateType.Add -> "Add"
                UpdateType.Remove -> "Remove"
            }
        )
    }
    @Throws(SerializationException::class)
    override fun deserialize(decoder: Decoder): UpdateType {
       return when(decoder.decodeString()){
           "Add" -> UpdateType.Add
           "Remove" -> UpdateType.Remove
           else ->  throw SerializationException()
       }
    }
}