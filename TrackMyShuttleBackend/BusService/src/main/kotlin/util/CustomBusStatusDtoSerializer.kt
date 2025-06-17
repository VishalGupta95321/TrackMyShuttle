package util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import model.BusStatusDto
import model.request.BusStopIdsUpdateRequest.Companion.UpdateType

object CustomBusStatusDtoSerializer : KSerializer<BusStatusDto> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BusStatusDto", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: BusStatusDto
    ) {
        encoder.encodeString(
            when(value) {
                BusStatusDto.Active -> "Active"
                BusStatusDto.InActive -> "InActive"
                BusStatusDto.InMaintenance -> "InMaintenance"
                BusStatusDto.NotInService -> "NotInService"
            }
        )
    }
    override fun deserialize(decoder: Decoder): BusStatusDto {
        return when(decoder.decodeString()){
            "Active" -> BusStatusDto.Active
            "InActive" -> BusStatusDto.InActive
            "InMaintenance" -> BusStatusDto.InMaintenance
            "NotInService" -> BusStatusDto.NotInService
            else ->  throw SerializationException()
        }
    }
}