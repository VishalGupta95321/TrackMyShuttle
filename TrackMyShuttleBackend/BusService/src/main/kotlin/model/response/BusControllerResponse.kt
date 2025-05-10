package model.response

import exceptions.BusControllerExceptions

typealias BusControllerResponse<T> = Response<T>

sealed interface Response<T> {
    data class Success<T>(val data: T?): BusControllerResponse<T>
    data class Error(val error : BusControllerExceptions): BusControllerResponse<Nothing>
}

