package controller

import data.exceptions.RouteRepoErrors
import data.respository.RouteRepository
import data.util.GetBack
import exceptions.RouteControllerExceptions
import model.response.BasicRouteControllerResponse
import model.response.BusIdsResponse
import model.response.RouteControllerResponse

class RouteControllerImpl(
    private val routeRepository: RouteRepository
): RouteController {
    override suspend fun assignRouteIdsToBus(
        busId: String,
        oldStops: List<String>,
        stopsToAdd: List<String>
    ): BasicRouteControllerResponse {
        if (stopsToAdd.count()+oldStops.count() != 2) return RouteControllerResponse.Error(RouteControllerExceptions.InvalidInput)
        val result = routeRepository.assignRoutesToBusAndUpdateBusTable(
            busId,
            oldStops,
            stopsToAdd,
        )
        return when(result) {
            is GetBack.Success -> RouteControllerResponse.Success()
            is GetBack.Error -> result.toRouteControllerErrors()
        }
    }

    override suspend fun deleteRouteIdsToBus(
        busId: String,
        allStops: List<String>,
        stopsToDelete: List<String>
    ): BasicRouteControllerResponse {
        if (allStops.isNotEmpty() && stopsToDelete.isNotEmpty())  return RouteControllerResponse.Error(RouteControllerExceptions.InvalidInput)

        val result = routeRepository.deleteAssignedRoutesForBusAndUpdateBusTable(
            busId,
            allStops,
            stopsToDelete,
        )
        return when(result) {
            is GetBack.Success -> RouteControllerResponse.Success()
            is GetBack.Error -> result.toRouteControllerErrors()
        }
    }

    override suspend fun getAllBusIdsByRouteIds(routeId: String): RouteControllerResponse<BusIdsResponse> {
        val result = routeRepository.getBusIdsByRouteId(routeId)
        return when(result) {
            is GetBack.Success ->  RouteControllerResponse.Success(
                result.data?.let { BusIdsResponse(it) }
            )
            is GetBack.Error -> result.toRouteControllerErrors()
        }
    }

    private fun GetBack.Error<RouteRepoErrors>.toRouteControllerErrors(): RouteControllerResponse.Error {
        return when (message) {
            is RouteRepoErrors.ItemNotExist -> RouteControllerResponse.Error(RouteControllerExceptions.ItemNotFound)
            is RouteRepoErrors.ItemAlreadyExists -> RouteControllerResponse.Error(RouteControllerExceptions.ItemAlreadyExists)
            else -> RouteControllerResponse.Error(RouteControllerExceptions.SomethingWentWrong)
        }
    }
}