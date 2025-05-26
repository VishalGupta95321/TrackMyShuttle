package model.response

import exceptions.RouteControllerExceptions

typealias BasicRouteControllerResponse = RouteControllerResponse<Nothing>


sealed interface RouteControllerResponse<out T> {
    data class Success<T>(val data: T? = null): RouteControllerResponse<T>
    data class Error(val error : RouteControllerExceptions): RouteControllerResponse<Nothing>
}