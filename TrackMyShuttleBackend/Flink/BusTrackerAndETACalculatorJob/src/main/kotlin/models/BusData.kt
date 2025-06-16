package models

import kotlinx.serialization.Serializable
import util.RouteType
import util.StopId
import util.WaitTime
import kotlin.time.Duration


@Serializable
data class BusData(
    val busId : String,
    val routeType : RouteType,
    val stopIds: List<Pair<StopId, WaitTime>>,
)
