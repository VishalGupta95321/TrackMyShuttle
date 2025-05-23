package data.util

import data.exceptions.BusStopRepoErrors
import data.exceptions.DynamoDbErrors


typealias GetBackBasic  = GetBack<Nothing, Nothing>

typealias DynamoDbResult<T> = GetBack<T, DynamoDbErrors>
typealias BasicDynamoDbResult = GetBack<Nothing, DynamoDbErrors>

typealias BusStopRepoResult<T> = GetBack<T, BusStopRepoErrors>
typealias BasicBusStopRepoResult = GetBack<Nothing, BusStopRepoErrors>


sealed interface GetBack< out T, out E> {
    data class Success<T>(val data: T? = null) : GetBack<T, Nothing>
    data class Error<E>(val message : E? = null) : GetBack<Nothing, E>
}
