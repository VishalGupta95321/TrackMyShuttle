package data.model

import data.entity.RouteEntity

data class Route(
    val routeId: String,
    val routeName: String?,
    val busIds: List<String>?,
)

fun Route.toRouteEntity(routeId: String): RouteEntity {
    return RouteEntity(
        routeId = routeId,
        busIds = busIds,
        routeName = routeName,
    )
}