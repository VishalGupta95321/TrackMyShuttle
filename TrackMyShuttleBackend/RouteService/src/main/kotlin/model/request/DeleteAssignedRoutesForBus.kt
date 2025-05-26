package model.request

data class DeleteAssignedRoutesForBus(
    val busId: String,
    val allStopsIds: List<String>,
    val stopsToDelete: List<String>
)