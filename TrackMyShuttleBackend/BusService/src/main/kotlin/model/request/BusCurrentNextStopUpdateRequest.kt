package model.request

data class BusCurrentNextStopUpdateRequest(
    val busId: String,
    val currentStopName: String,
    val nextStopName: String,
)