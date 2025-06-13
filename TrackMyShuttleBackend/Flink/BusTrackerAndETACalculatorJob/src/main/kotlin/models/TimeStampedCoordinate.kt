package models

import kotlinx.serialization.Serializable

@Serializable
data class TimeStampedCoordinate(
    val coordinate: Coordinate,
    val timestamp: Long,
)

fun TimeStampedCoordinate.toCoordinate() = Coordinate(
    latitude = coordinate.latitude,
    longitude = coordinate.longitude
)
