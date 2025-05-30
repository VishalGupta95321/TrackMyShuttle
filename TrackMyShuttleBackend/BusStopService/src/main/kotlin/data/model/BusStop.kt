package data.model

import data.entity.BusStopEntity

data class BusStop(
    val stopId: String,
    val stopName: String,
    val address: String,
    val location: Location,
)

fun BusStop.toBusStopEntity(): BusStopEntity {
    return BusStopEntity(
        stopId = stopId,
        stopName = stopName,
        address = address,
        location = location,
    )
}