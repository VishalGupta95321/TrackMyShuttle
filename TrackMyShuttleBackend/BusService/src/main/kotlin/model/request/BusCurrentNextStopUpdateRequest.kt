package model.request

import kotlinx.serialization.Serializable

@Serializable
data class BusCurrentNextStopUpdateRequest(
    val currentStopName: String,
    val nextStopName: String,
)