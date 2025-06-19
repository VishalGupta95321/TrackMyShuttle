package data.respository

import data.util.RouteRepoResult
import org.example.data.model.Route
import org.example.data.util.RouteType

interface RouteRepository {
    suspend fun fetchRoutesByIds(ids: List<String>): RouteRepoResult<Route>
    suspend fun createRoutes(stopIds: List<String>, routeType: RouteType): RouteRepoResult<List<String>>
    suspend fun deleteRoutes(stopIds: List<String>, routeCount: Int): RouteRepoResult<String>
}

