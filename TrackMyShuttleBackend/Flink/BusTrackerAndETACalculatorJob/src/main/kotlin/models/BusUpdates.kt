package models

import kotlinx.serialization.Serializable


@Serializable
data class BusUpdates(
    val busId: String,
    val nextStopId: String,
    val lastStopId: String
)