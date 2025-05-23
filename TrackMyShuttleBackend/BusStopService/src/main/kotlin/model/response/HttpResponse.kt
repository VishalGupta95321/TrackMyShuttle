package model.response

import exceptions.BusStopControllerExceptions

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


fun <T> generateHttpResponse(response: BusStopControllerResponse<T>): HttpResponse<T> {
    return when(response){
        is BusStopControllerResponse.Success -> HttpResponse(code = HttpStatusCode.OK, body = response.data)
        is BusStopControllerResponse.Error ->  {
            when(response.error){
                BusStopControllerExceptions.SomethingWentWrong -> HttpResponse(code = HttpStatusCode.INTERNAL_SERVER_ERROR)
                BusStopControllerExceptions.InvalidInput -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                BusStopControllerExceptions.ItemAlreadyExists -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                BusStopControllerExceptions.ItemNotFound -> HttpResponse(code = HttpStatusCode.NOT_FOUND)
            }
        }
    }
}