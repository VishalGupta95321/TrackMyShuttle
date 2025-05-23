package model.response

import data.model.BusStop
import model.LocationDto
import model.LocationDto.Companion.fromLocation

data class BusStopDto(
    val stopID: String,
    val stopName: String,
    val address: String,
    val location: LocationDto,
){

    fun toBusStop(): BusStop = BusStop(
        stopId = stopID,
        stopName = stopName,
        address = address,
        location = location.toLocation(),
    )
    companion object {
        fun fromBusStop(busStop: BusStop): BusStopDto {
            return BusStopDto(
                stopID = busStop.stopId,
                stopName = busStop.stopName,
                address = busStop.address,
                location = fromLocation(busStop.location)
            )
        }
    }
}


data class BusStopResponse(
    val busStop: BusStopDto
)