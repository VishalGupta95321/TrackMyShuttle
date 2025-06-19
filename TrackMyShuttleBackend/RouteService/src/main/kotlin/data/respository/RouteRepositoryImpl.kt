package data.respository

import data.entity.RouteEntity
import data.exceptions.DynamoDbErrors
import data.exceptions.RouteRepoErrors
import org.example.data.source.dynamo_db.DynamoDbDataSource
import data.util.GetBack
import data.util.RouteRepoResult
import org.example.data.model.Route
import org.example.data.util.RouteType

class RouteRepositoryImpl(
    private val dynamoDbSource: DynamoDbDataSource<RouteEntity>
): RouteRepository {

    override suspend fun fetchRoutesByIds(ids: List<String>): RouteRepoResult<Route> {
        TODO("Not yet implemented")
    }

    override suspend fun createRoutes(
        stopIds: List<String>,
        routeType: RouteType
    ): RouteRepoResult<List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRoutes(
        stopIds: List<String>,
        routeCount: Int
    ): RouteRepoResult<String> {
        TODO("Not yet implemented")
    }

    private fun GetBack.Error<DynamoDbErrors>.toRouteRepoErrors(): GetBack.Error<RouteRepoErrors>{
        return when(message) {
            is DynamoDbErrors.ItemDoesNotExists -> GetBack.Error(RouteRepoErrors.RouteDoesNotExist)
            is DynamoDbErrors.ItemAlreadyExists ->  GetBack.Error(RouteRepoErrors.RouteAlreadyExists)
            else ->  GetBack.Error(RouteRepoErrors.SomethingWentWrong)
        }
    }

}