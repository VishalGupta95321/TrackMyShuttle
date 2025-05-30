package model.response

import data.model.BusStop
import kotlinx.serialization.Serializable
import model.LocationDto
import model.LocationDto.Companion.fromLocation


@Serializable
data class BusStopDto(
    val stopId: String,
    val stopName: String,
    val address: String,
    val location: LocationDto,
){

    fun toBusStop(): BusStop = BusStop(
        stopId = stopId,
        stopName = stopName,
        address = address,
        location = location.toLocation(),
    )
    companion object {
        fun fromBusStop(busStop: BusStop): BusStopDto {
            return BusStopDto(
                stopId = busStop.stopId,
                stopName = busStop.stopName,
                address = busStop.address,
                location = fromLocation(busStop.location)
            )
        }
    }
}

@Serializable
data class BusStopResponse(
    val busStop: BusStopDto
)