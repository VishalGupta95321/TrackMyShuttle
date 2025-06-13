package util.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.*
import models.EtaResult


object CustomEtaResultSerializer : KSerializer<EtaResult> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EtaResult") {
        element("kind", serialDescriptor<String>())
        element("etaInSec", serialDescriptor<Long>())
        element("deltaInSec", serialDescriptor<Long>())
        element("remainingDistance", serialDescriptor<String>())
    }


    override fun serialize(encoder: Encoder, value: EtaResult) {
        encoder.encodeStructure(descriptor) {
            when (value) {
                is EtaResult.Ahead -> {
                    encodeStringElement(descriptor, 0, "Ahead")
                    encodeLongElement(descriptor, 1, value.etaInSec)
                    encodeLongElement(descriptor, 2, value.deltaInSec)
                    encodeStringElement(descriptor, 3, value.remainingDistance)
                }

                is EtaResult.Delayed -> {
                    encodeStringElement(descriptor, 0, "Delayed")
                    encodeLongElement(descriptor, 1, value.etaInSec)
                    encodeLongElement(descriptor, 2, value.deltaInSec)
                    encodeStringElement(descriptor, 3, value.remainingDistance)
                }
            }
        }
    }

    override fun deserialize(decoder: Decoder): EtaResult {
        return decoder.decodeStructure(descriptor) {
            var kind: String? = null
            var eta: Long? = null
            var delta: Long? = null
            var remDis: String? = null

            loop@ while (true) {
                when (decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> kind = decodeStringElement(descriptor, 0)
                    1 -> eta = decodeLongElement(descriptor, 1)
                    2 -> delta = decodeLongElement(descriptor, 2)
                    3 -> remDis = decodeStringElement(descriptor, 3)
                    else -> throw SerializationException()
                }
            }
            if (kind == null || eta == null || delta == null || remDis == null){
                throw SerializationException()
            }
            when (kind) {
                "Ahead" -> EtaResult.Ahead(eta, delta, remDis)
                "Delayed" -> EtaResult.Delayed(eta, delta, remDis)
                else -> throw SerializationException()
            }
        }
    }
}