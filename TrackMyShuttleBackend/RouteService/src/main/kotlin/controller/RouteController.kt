package controller

import data.model.BasicRouteDetails
import data.util.BasicRouteRepoResult
import model.response.BasicRouteControllerResponse
import model.response.BusIdsResponse
import model.response.RouteControllerResponse

interface RouteController {

    suspend fun assignRouteIdsToBus(
        busId: String,
        oldStops: List<String>,
        stopsToAdd: List<String>
    ): BasicRouteControllerResponse

    suspend fun deleteRouteIdsToBus(
        busId: String,
        allStops: List<String>,
        stopsToDelete: List<String>
    ): BasicRouteControllerResponse

    suspend fun getAllBusIdsByRouteIds(routeId: String): RouteControllerResponse<BusIdsResponse>

}