package models


data class WindowedCoordinates(
    val busId: String,
    val oldestCoordinates: Coordinate,
    val latestCoordinates: Coordinate
)
