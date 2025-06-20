package model.response

import kotlinx.serialization.Serializable


@Serializable
data class BusStopsResponse(
    val busStops: List<BusStopDto>
)