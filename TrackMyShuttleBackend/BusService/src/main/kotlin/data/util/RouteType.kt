package data.util

import kotlinx.serialization.Serializable

@Serializable(with = CustomRouteTypeSerializer::class)
sealed interface RouteType{
    data object OutAndBack : RouteType
    data object Loop : RouteType
}