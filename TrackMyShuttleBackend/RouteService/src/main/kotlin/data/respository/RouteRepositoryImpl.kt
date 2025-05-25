package data.respository

import data.entity.RouteEntity
import data.exceptions.DynamoDbErrors
import data.exceptions.RouteRepoErrors
import data.model.DynamoDbTransactWriteItem
import data.source.DynamoDbDataSource
import data.util.BasicRouteRepoResult
import data.util.GetBack
import data.util.RouteRepoResult


class RouteRepositoryImpl(
    private val dynamoDbSource: DynamoDbDataSource<RouteEntity>
): RouteRepository {

    override suspend fun createRoutes(stopIds: List<String>): BasicRouteRepoResult {
        val routeEntities = generatePossibleRoutes(stopIds).map { RouteEntity(it,null) }
        val result = dynamoDbSource.transactWriteItems( items =
            routeEntities.map { DynamoDbTransactWriteItem(putItem = it, deleteItemKey = null, updateItem = null)}
        )
        return when (result) {
            is GetBack.Error -> result.toRouteRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun deleteRoutes(stopIds: List<String>): BasicRouteRepoResult {

        val routeEntities = generatePossibleRoutes(stopIds).map { RouteEntity(it,null) }

        val result = dynamoDbSource.transactWriteItems( items =
            routeEntities.map { DynamoDbTransactWriteItem(putItem = null, deleteItemKey = it.routeId, updateItem = null)}
        )
        return when (result) {
            is GetBack.Error -> result.toRouteRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun assignRoutesToBusAndUpdateBusTable(
        busId: String,
        stopsIds: List<String>
    ): BasicRouteRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAssignedRoutesForBusAndUpdateBusTable(
        busId: String,
        stopIds: List<String>
    ): BasicRouteRepoResult {
        TODO("Not yet implemented")
    }

    override suspend fun getBusIdsByRouteId(routeId: String): RouteRepoResult<List<String>> {
        TODO("Not yet implemented")
    }

    private fun generatePossibleRoutes(stopIds: List<String>): List<String> {
        val routes  = mutableListOf<String>()
        for (i in stopIds.indices) {
            for (j in stopIds.indices) {
                if (i != j) {
                    routes.add("${stopIds[i]+ "-" +stopIds[j]} ")
                    println("${stopIds[i]+ "-" +stopIds[j]} ")
                }
            }
        }
        return routes
    }

    private fun GetBack.Error<DynamoDbErrors>.toRouteRepoErrors(): GetBack.Error<RouteRepoErrors>{
        return when(message) {
            is DynamoDbErrors.ItemDoesNotExists -> GetBack.Error(RouteRepoErrors.BusStopDoesNotExist)
            is DynamoDbErrors.ItemAlreadyExists ->  GetBack.Error(RouteRepoErrors.BusStopAlreadyExists)
            else ->  GetBack.Error(RouteRepoErrors.SomethingWentWrong)
        }
    }/// TODO()
}