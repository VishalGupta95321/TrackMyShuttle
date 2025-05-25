package data.util

import data.exceptions.RouteRepoErrors
import data.exceptions.DynamoDbErrors


typealias GetBackBasic  = GetBack<Nothing, Nothing>

typealias DynamoDbResult<T> = GetBack<T, DynamoDbErrors>
typealias BasicDynamoDbResult = GetBack<Nothing, DynamoDbErrors>

typealias RouteRepoResult<T> = GetBack<T, RouteRepoErrors>
typealias BasicRouteRepoResult = GetBack<Nothing, RouteRepoErrors>


sealed interface GetBack< out T, out E> {
    data class Success<T>(val data: T? = null) : GetBack<T, Nothing>
    data class Error<E>(val message : E? = null) : GetBack<Nothing, E>
}
