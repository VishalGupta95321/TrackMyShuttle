package models

import kotlinx.serialization.Serializable
import util.serializers.CustomEtaResultSerializer

@Serializable
data class BusETAData(
    val busId: String,
    val eta: EtaResult,
)

@Serializable(with = CustomEtaResultSerializer::class)
sealed interface EtaResult {
    data class Ahead(val etaInSec: Long, val deltaInSec: Long, val remainingDistance: String) : EtaResult
    data class Delayed(val etaInSec: Long, val deltaInSec: Long, val remainingDistance: String) : EtaResult
}
