package model.response

import data.model.BusStopScanned
import kotlinx.serialization.Serializable


@Serializable
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

@Serializable
data class BusStopScannedResponse(
    val busStops: List<BusStopScannedDto>
)
