package model.response

import exceptions.BusStopControllerExceptions

typealias BasicBusStopControllerResponse = BusStopControllerResponse<Nothing>


sealed interface BusStopControllerResponse<out T> {
    data class Success<T>(val data: T? = null): BusStopControllerResponse<T>
    data class Error(val error : BusStopControllerExceptions): BusStopControllerResponse<Nothing>
}