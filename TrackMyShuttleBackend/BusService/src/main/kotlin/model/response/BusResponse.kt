package model.response

import data.model.Bus
import kotlinx.serialization.Serializable
import model.BusStatusDto
import model.BusStatusDto.Companion.fromBusStatus
import util.CustomBusStatusDtoSerializer

@Serializable
data class BusDto(
    val busId:String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatusDto?,
    val currentStop: String?,  // these two fields will be added by server or wherever
    val nextStop: String?,
    val stopIds: List<String>,
){
    companion object{
        fun fromBus(bus: Bus): BusDto{
            return BusDto(
                bus.busId,
                bus.driverName,
                bus.activeHours,
                bus.activeDays,
                bus.busStatus?.let { fromBusStatus(it) },
                bus.currentStop,
                bus.nextStop,
                bus.stopIds,
            )
        }
    }
}

@Serializable
data class BusResponse(
    val bus: BusDto
)
