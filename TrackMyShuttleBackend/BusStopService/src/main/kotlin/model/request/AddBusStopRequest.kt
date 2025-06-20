package model.request

import data.model.BusStop
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import model.LocationDto
import model.response.BusStopDto

@Serializable
data class AddBusStopRequest(
    val stopId: String,
    val stopName: String,
    val address: String,
    val location: LocationDto,
    val radiusInMeters: Double,
){
    fun toBusStop(): BusStop = BusStop(
        stopId = stopId,
        stopName = stopName,
        address = address,
        location = location.toLocation(),
        radiusInMeters = radiusInMeters,
    )
}


/// FIXME change it to Add Bus Stop and BatchAddBus stop

