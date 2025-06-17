package model.request

import kotlinx.serialization.Serializable
import util.CustomBusStatusDtoSerializer
import util.CustomUpdateTypeSerializer

@Serializable()
data class BusStopIdsUpdateRequest(
    val busId: String,
    val stopIds: List<StopIdsWIthWaitTimeDto>,
    val updateType: UpdateType
){

    companion object {
        @Serializable(with = CustomUpdateTypeSerializer::class)
        sealed interface UpdateType {
            object Add: UpdateType
            object Remove: UpdateType
        }
    }
}

