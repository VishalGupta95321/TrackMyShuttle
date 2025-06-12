package models

import kotlinx.serialization.Serializable
import util.RouteType
import kotlin.time.Duration


@Serializable
data class BusData(
    val busId : String,
    val routeType : RouteType,
    val stops : List<BusStop>
)
