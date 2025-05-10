package model.response

import model.BusStatusDto

data class BusDto(
    val busId:String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatusDto,
    val currentStop: String?,  // these two fields will be added by server or wherever
    val nextStop: String?,
)

data class BusResponse(
    val bus: BusDto
)
