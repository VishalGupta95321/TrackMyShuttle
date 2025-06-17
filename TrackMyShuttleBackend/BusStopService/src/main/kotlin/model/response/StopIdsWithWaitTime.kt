package model.response

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class StopIdsWithWaitTime(
    val stopId: String,
    val waitTime: Duration,
)