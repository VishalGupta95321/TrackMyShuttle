package model.response

import data.model.BusStopScanned

data class BusStopScannedDto(
    val stopId: String,
    val stopName: String,
    val address: String,
){
    companion object {
        fun fromBusStopScanned(stop: BusStopScanned) = BusStopScannedDto(
            stopId = stop.stopId,
            stopName = stop.stopName,
            address = stop.address,
        )
    }
}

data class BusStopScannedResponse(
    val busStops: List<BusStopScannedDto>?
)
