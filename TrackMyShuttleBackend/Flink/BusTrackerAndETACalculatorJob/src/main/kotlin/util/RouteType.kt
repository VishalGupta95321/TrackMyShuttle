package util

import kotlinx.serialization.Serializable
import util.serializers.CustomRouteTypeSerializer


@Serializable(with = CustomRouteTypeSerializer::class)
sealed interface RouteType{
    data object OutAndBack : RouteType
    data object Loop : RouteType
}