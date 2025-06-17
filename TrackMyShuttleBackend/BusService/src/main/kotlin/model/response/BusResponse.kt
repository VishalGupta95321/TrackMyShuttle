package model.response

import data.model.Bus
import kotlinx.serialization.Serializable
import model.BusStatusDto
import model.BusStatusDto.Companion.fromBusStatus
import model.RouteTypeDto
import model.RouteTypeDto.Companion.fromRouteType
import model.request.StopIdsWIthWaitTimeDto
import model.request.StopIdsWIthWaitTimeDto.Companion.fromStopIdsWithWaitTime
import util.CustomBusStatusDtoSerializer

@Serializable
data class BusDto(
    val busId:String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatusDto?,
    val routeType: RouteTypeDto,
    val currentStop: String?,  // these two fields will be added by server or wherever
    val nextStop: String?,
    val stopIds: List<StopIdsWIthWaitTimeDto>,
){
    companion object{
        fun fromBus(bus: Bus): BusDto{
            return BusDto(
                bus.busId,
                bus.driverName,
                bus.activeHours,
                bus.activeDays,
                bus.busStatus?.let { fromBusStatus(it) },
                fromRouteType(bus.routeType),
                bus.currentStop,
                bus.nextStop,
                bus.stopIds.map { fromStopIdsWithWaitTime(it) },
            )
        }
    }
}

@Serializable
data class BusResponse(
    val bus: BusDto
)
