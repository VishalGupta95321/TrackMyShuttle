package models

import kotlinx.serialization.Serializable
import util.RouteType
import kotlin.time.Duration


@Serializable
data class BusData(
    val busId : String,
    val routeType : RouteType,  // TODO("Update Service")
    val stopIds: List<String>,
)
