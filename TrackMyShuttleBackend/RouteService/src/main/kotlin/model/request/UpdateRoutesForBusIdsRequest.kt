package model.request

data class UpdateRoutesForBusIdsRequest(
    val busId: String,
    val allStopIds: List<String>,
    val stopIdsToUpdate : List<String>,
    val updateType: RoutesUpdateType
){
    companion object {
        sealed interface RoutesUpdateType{
            data object Add : RoutesUpdateType
            data object Remove : RoutesUpdateType
        }
    }
}
