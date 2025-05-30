package model.response

import kotlinx.serialization.Serializable

@Serializable
data class BusIdsResponse(
    val busIds: List<String>
)