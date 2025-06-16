package models

data class BusLocationWithMetadataWindowed(
    val busId: String,
    val busLocationMetadata: BusLocationWithMetadata,
    val oldestCoordinates: TimeStampedCoordinate,
    val latestCoordinates: TimeStampedCoordinate
)
