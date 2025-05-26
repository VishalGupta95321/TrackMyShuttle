package model.request

data class AssignRoutesToBusIdRequest(
    val oldStopIds: List<String>,
    val stopIdsToAdd: List<String>,
    val busId: String
)
