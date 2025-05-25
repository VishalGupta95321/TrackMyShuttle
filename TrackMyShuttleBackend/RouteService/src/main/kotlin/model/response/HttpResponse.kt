package model.response

import exceptions.RouteControllerExceptions

data class HttpResponse<T>(
    val code: String,
    val body: T? = null,
)

object HttpStatusCode{
    const val OK = "200"
    const val BAD_REQUEST = "400"
    const val INTERNAL_SERVER_ERROR = "500"
    const val NOT_FOUND = "404"
}


fun <T> generateHttpResponse(response: RouteControllerResponse<T>): HttpResponse<T> {
    return when(response){
        is RouteControllerResponse.Success -> HttpResponse(code = HttpStatusCode.OK, body = response.data)
        is RouteControllerResponse.Error ->  {
            when(response.error){
                RouteControllerExceptions.SomethingWentWrong -> HttpResponse(code = HttpStatusCode.INTERNAL_SERVER_ERROR)
                RouteControllerExceptions.InvalidInput -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                RouteControllerExceptions.ItemAlreadyExists -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                RouteControllerExceptions.ItemNotFound -> HttpResponse(code = HttpStatusCode.NOT_FOUND)
            }
        }
    }
}