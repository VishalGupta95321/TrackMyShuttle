package model

import data.model.Location
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val latitude: String,
    val longitude: String,
){
    fun toLocation() = Location(latitude, longitude)


    companion object {
        fun fromLocation(location: Location) = LocationDto(
            location.latitude,
            location.longitude,
        )
    }

}