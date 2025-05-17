package model.response

import exceptions.BusControllerExceptions

typealias BasicBusControllerResponse = BusControllerResponse<Nothing>

sealed interface BusControllerResponse<out T> {
    data class Success<T>(val data: T? = null): BusControllerResponse<T>
    data class Error(val error : BusControllerExceptions): BusControllerResponse<Nothing>
}

