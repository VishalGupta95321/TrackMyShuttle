package model.request

data class BusCurrentNextStopUpdateRequest(
    val busId: String,
    val currentStopId: String,
    val nextStopId: String,
)