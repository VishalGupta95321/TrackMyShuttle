package data.respository

import data.util.BasicRouteRepoResult
import data.util.RouteRepoResult


interface RouteRepository {

    suspend fun createRoutes(oldStopIds: List<String>, newStopIds: List<String>): RouteRepoResult<List<String>>  /// return route ids which will then we will feed to assignRoutes
    suspend fun deleteRoutes(stopIds: List<String>): BasicRouteRepoResult /// used along with BusStopDelete using transact write

    suspend fun assignRoutesToBusAndUpdateBusTable(
        busId: String,
        oldStopIds: List<String>,
        newStopsIds: List<String>
    ): BasicRouteRepoResult // at least minimum 2 stops

    suspend fun deleteAssignedRoutesForBusAndUpdateBusTable(
        busId: String,
        allStopIds: List<String>,
        stopIdsToDelete: List<String>
    ): BasicRouteRepoResult // at least one stop Ids

    suspend fun getBusIdsByRouteId(routeId: String): RouteRepoResult<List<String>>

}