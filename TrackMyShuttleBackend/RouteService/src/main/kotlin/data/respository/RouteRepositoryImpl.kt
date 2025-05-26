package data.respository

import data.entity.RouteEntity
import data.exceptions.DynamoDbErrors
import data.exceptions.RouteRepoErrors
import data.model.DynamoDbTransactWriteItem
import data.model.DynamoDbTransactWriteItem.Companion.TransactionUpdateItem
import data.source.DynamoDbDataSource
import data.util.BasicRouteRepoResult
import data.util.BusEntityAttrUpdate
import data.util.BusEntityAttrUpdate.UpdateStopIds.Companion.StopIdsUpdateAction
import data.util.GetBack
import data.util.RouteEntityAttrUpdate
import data.util.RouteEntityAttrUpdate.UpdateBusIds.Companion.BusIdsUpdateAction
import data.util.RouteRepoResult


private const val PLACEHOLDER_BUS_ID_FOR_ROUTE = "NONE"

class RouteRepositoryImpl(
    private val dynamoDbSource: DynamoDbDataSource<RouteEntity>
): RouteRepository {

    override suspend fun createRoutes(
        oldStopIds: List<String>,
        stopIdsToAdd: List<String>
    ): RouteRepoResult<List<String>> {

        var routes = mutableListOf<String>()

        //// Generate all the routes including all stops and removes the ones including old stops.
        routes.apply {
            addAll(generatePossibleRoutes(oldStopIds+stopIdsToAdd))
            removeAll(generatePossibleRoutes(oldStopIds))
        }
        val routeEntities = routes.map { RouteEntity(routeId = it,busIds = listOf(PLACEHOLDER_BUS_ID_FOR_ROUTE)) }

        routeEntities.forEach { item ->
            val result = dynamoDbSource.putItem(item)
            when (result) {
                is GetBack.Error -> {
                    if (result.message !is DynamoDbErrors.ItemAlreadyExists){
                        return result.toRouteRepoErrors()
                    }
                }
                is GetBack.Success -> {}
            }
        }
        return GetBack.Success(routes)
    }

    override suspend fun deleteRoutes(stopIds: List<String>): BasicRouteRepoResult {

        val routeEntities = generatePossibleRoutes(stopIds).map { RouteEntity(it,listOf()) }

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
        oldStopIds: List<String>,   //// can be empty
        stopIdsToAdd: List<String>  /// cant be empty /// at least 2 stop ids with or without old stops
    ): BasicRouteRepoResult {

        val routes = createRoutes(oldStopIds, stopIdsToAdd).let { result->
            when (result) {
                is GetBack.Error -> return GetBack.Error()
                is GetBack.Success -> result.data

            }
        }

        val routeTableWriteItems = routes?.map { routeId ->
            DynamoDbTransactWriteItem<RouteEntity>(
                putItem = null,
                deleteItemKey = null,
                updateItem = TransactionUpdateItem(
                    key = routeId,
                    attrToUpdate = RouteEntityAttrUpdate.UpdateBusIds(
                        keyVal = routeId,
                        value = listOf(busId),
                        updateAction = BusIdsUpdateAction.Add
                    )
                )
            )
        }

        val busTableWriteItems = DynamoDbTransactWriteItem<RouteEntity>(
            putItem = null,
            deleteItemKey = null,
            updateItem = TransactionUpdateItem(
                key = busId,
                attrToUpdate = BusEntityAttrUpdate.UpdateStopIds(
                    keyVal = busId,
                    value = stopIdsToAdd,
                    updateAction =  StopIdsUpdateAction.Add
                )
            )
        )

        val allWriteItems = mutableListOf<DynamoDbTransactWriteItem<RouteEntity>>()
        routeTableWriteItems?.let { allWriteItems.addAll(it) }
        allWriteItems.add(busTableWriteItems)

        val result = dynamoDbSource.transactWriteItems(items = allWriteItems)
        return when (result) {
            is GetBack.Error -> result.toRouteRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun deleteAssignedRoutesForBusAndUpdateBusTable(
        busId: String,
        allStopIds: List<String>,
        stopIdsToDelete: List<String>
    ): BasicRouteRepoResult {

        val allRoutes = generatePossibleRoutes(allStopIds)
        val routesExclDelStops = generatePossibleRoutes(allStopIds.minus(stopIdsToDelete))
        val routesToDelete = allRoutes.minus(routesExclDelStops)

        val dbWriteItems = routesToDelete.map { routeId ->
            DynamoDbTransactWriteItem<RouteEntity>(
                putItem = null,
                deleteItemKey = null,
                updateItem = TransactionUpdateItem(
                    key = routeId,
                    attrToUpdate = RouteEntityAttrUpdate.UpdateBusIds(
                        keyVal = routeId,
                        value = listOf(busId),
                        updateAction = BusIdsUpdateAction.Delete
                    )
                )
            )
        }


        val result = dynamoDbSource.transactWriteItems(items = dbWriteItems)
        return when (result) {
            is GetBack.Error -> result.toRouteRepoErrors()
            is GetBack.Success -> GetBack.Success()
        }
    }

    override suspend fun getBusIdsByRouteId(routeId: String): RouteRepoResult<List<String>> {
        val result = dynamoDbSource.getItem(routeId)
        return when (result) {
            is GetBack.Error -> result.toRouteRepoErrors()
            is GetBack.Success -> {
                val busIds = result.data?.busIds?.minus(PLACEHOLDER_BUS_ID_FOR_ROUTE)
                GetBack.Success(busIds)
            }
        }
    }

    private fun generatePossibleRoutes(
        stopIds: List<String>,
    ): List<String> {

        val routes  = mutableListOf<String>()
        stopIds.forEachIndexed { i, _ ->
            stopIds.forEachIndexed { j, _ ->
                if (i < j) {
                    routes.add("${stopIds[i]+ "-" + stopIds[j]} ")
                }
            }
        }
        return routes
    }


    private fun GetBack.Error<DynamoDbErrors>.toRouteRepoErrors(): GetBack.Error<RouteRepoErrors>{
        return when(message) {
            is DynamoDbErrors.ItemDoesNotExists -> GetBack.Error(RouteRepoErrors.ItemNotExist)
            is DynamoDbErrors.ItemAlreadyExists ->  GetBack.Error(RouteRepoErrors.ItemAlreadyExists)
            else ->  GetBack.Error(RouteRepoErrors.SomethingWentWrong)
        }
    }
}