package model.request

import data.model.StopIdsWithWaitTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class StopIdsWIthWaitTimeDto(
    val stopId: String,
    val waitTime: Duration,
){
    fun toStopIdsWithWaitTime() = StopIdsWithWaitTime(stopId, waitTime)
    companion object {
        fun fromStopIdsWithWaitTime(value: StopIdsWithWaitTime) = StopIdsWIthWaitTimeDto(value.stopId, value.waitTime)
    }
}