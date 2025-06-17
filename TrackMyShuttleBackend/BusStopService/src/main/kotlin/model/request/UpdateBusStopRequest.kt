package model.request

import data.model.BusStop
import kotlinx.serialization.Serializable
import model.LocationDto
import model.response.BusStopDto

@Serializable
data class UpdateBusStopRequest(
    val stopName: String,
    val address: String,
    val location: LocationDto,
    val radiusInMeters: Double,
){
    fun toBusStop(stopId: String): BusStop {
        return BusStop(
            stopId = stopId,
            stopName = stopName,
            address = address,
            location = location.toLocation(),
            radiusInMeters = radiusInMeters,
        )
    }
}