package model.request

import kotlinx.serialization.Serializable
import model.response.BusStopDto


@Serializable
data class BatchAddBusStopRequest(
    val busStops: List<BusStopDto>
)
