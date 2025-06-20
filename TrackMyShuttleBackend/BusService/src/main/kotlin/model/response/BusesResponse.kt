package model.response

import kotlinx.serialization.Serializable
import util.CustomBusStatusDtoSerializer

@Serializable
data class BusesResponse(
    val buses: List<BusDto>
)