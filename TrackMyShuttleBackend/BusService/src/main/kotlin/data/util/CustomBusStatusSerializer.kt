package data.util

import data.model.BusStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import model.BusStatusDto

object CustomBusStatusSerializer : KSerializer<BusStatus> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BusStatus", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: BusStatus
    ) {
        encoder.encodeString(
            when(value) {
                BusStatus.Active -> "Active"
                BusStatus.InActive -> "InActive"
                BusStatus.InMaintenance -> "InMaintenance"
                BusStatus.NotInService -> "NotInService"
            }
        )
    }
    override fun deserialize(decoder: Decoder): BusStatus {
        return when(decoder.decodeString()){
            "Active" -> BusStatus.Active
            "InActive" -> BusStatus.InActive
            "InMaintenance" -> BusStatus.InMaintenance
            "NotInService" -> BusStatus.NotInService
            else ->  throw SerializationException()
        }
    }
}