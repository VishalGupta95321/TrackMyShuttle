package models

import com.mapbox.geojson.Point
import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(
    val latitude: String,
    val longitude: String,
)
fun Coordinate.toPoint(): Point = Point.fromLngLat(this.longitude.toDouble(),this.latitude.toDouble())
