package model

import data.model.Location


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