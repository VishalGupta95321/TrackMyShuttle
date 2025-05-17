package model.request

data class BusStopIdsUpdateRequest(
    val busId: String,
    val stopIds: List<String>,
    val updateType: UpdateType,
){
    companion object {
        sealed interface UpdateType {
            data object Add: UpdateType
            data object Remove: UpdateType
        }
    }
}