package model.response

import kotlinx.serialization.Serializable
import model.LocationDto

@Serializable
data class UpdatedBusStopResponse(
    val stopName: String,
    val address: String,
    val location: LocationDto,
    val radiusInMeters: Double,
)