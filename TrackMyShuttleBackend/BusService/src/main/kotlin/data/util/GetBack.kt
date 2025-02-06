package data.util

import data.exceptions.BusDataRepoErrors
import data.exceptions.DynamoDbErrors


typealias GetBackBasic  = GetBack<Nothing, Nothing>

typealias DynamoDbResult<T> = GetBack<T, DynamoDbErrors>
typealias BasicDynamoDbResult = GetBack<Nothing, DynamoDbErrors>

typealias BusRepoResult<T> = GetBack<T, BusDataRepoErrors>
typealias BasicBusRepoResult<T> = GetBack<Nothing, BusDataRepoErrors>


sealed interface GetBack<out T, out E> {
    data class Success<T>(val data: T? = null) : GetBack<T, Nothing>
    data class Error<E>(val message : E? = null) : GetBack<Nothing, E>
}
