package models


data class WindowedCoordinates(
    val busId: String,
    val oldestCoordinates: Coordinates,
    val latestCoordinates: Coordinates
)
