package data.respository

import data.entity.RouteEntity
import data.util.BasicRouteRepoResult
import data.util.RouteRepoResult


interface RouteRepository {
    suspend fun createRoutes(stopIds: List<String>): BasicRouteRepoResult
    suspend fun deleteRoutes(stopIds: List<String>): BasicRouteRepoResult /// used along with BusStopDelete using transact write

    suspend fun assignRoutesToBusAndUpdateBusTable(busId: String, stopsIds: List<String>): BasicRouteRepoResult // at least minimum 2 stops //if REGISTERING ROUTE, rest any amount in UPDATE ROUTE .
    suspend fun deleteAssignedRoutesForBusAndUpdateBusTable(busId: String, stopIds: List<String>): BasicRouteRepoResult // at least one stop Ids
    suspend fun getBusIdsByRouteId(routeId: String): RouteRepoResult<List<String>>
}