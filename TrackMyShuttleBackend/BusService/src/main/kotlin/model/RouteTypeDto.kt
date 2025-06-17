package model

import data.util.CustomRouteTypeSerializer
import data.util.RouteType
import kotlinx.serialization.Serializable
import util.CustomRouteTypeDtoSerializer

@Serializable(with = CustomRouteTypeDtoSerializer::class)
sealed interface RouteTypeDto{
    data object OutAndBack : RouteTypeDto
    data object Loop : RouteTypeDto

    fun toRouteType() : RouteType {
        return when(this){
            Loop -> RouteType.Loop
            OutAndBack -> RouteType.OutAndBack
        }
    }

    companion object{
        fun fromRouteType(routeType: RouteType) : RouteTypeDto {
            return when(routeType){
                RouteType.Loop -> Loop
                RouteType.OutAndBack -> OutAndBack
            }
        }
    }
}