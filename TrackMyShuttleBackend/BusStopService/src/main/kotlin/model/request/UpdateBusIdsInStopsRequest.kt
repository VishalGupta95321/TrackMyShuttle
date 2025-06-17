package model.request

import kotlinx.serialization.Serializable
import model.response.StopIdsWithWaitTime
import util.CustomUpdateTypeSerializer


@Serializable
data class UpdateBusIdsInStopsRequest(
    val busId: String,
    val stopIds: List<StopIdsWithWaitTime>,
    val updateType: UpdateType
){
    companion object {
        @Serializable(with = CustomUpdateTypeSerializer::class)
        sealed interface UpdateType {
            data object Add: UpdateType
            data object Remove: UpdateType
        }
    }
}
