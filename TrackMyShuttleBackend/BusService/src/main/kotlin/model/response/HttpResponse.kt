package model.response

import exceptions.BusControllerExceptions

data class HttpResponse<T>(
    val code: String,
    val body: T? = null,
)

object HttpStatusCode{
    const val OK = "200"
}


fun <T> generateHttpResponse(response: BusControllerResponse<T>): HttpResponse<T> {
    return when(response){
        is BusControllerResponse.Success -> HttpResponse(code = HttpStatusCode.OK, body = response.data)
        is BusControllerResponse.Error ->  {
            when(response){
                BusControllerExceptions.SomethingWentWrong -> HttpResponse(code = HttpStatusCode.OK)
                else -> HttpResponse(code = HttpStatusCode.OK)
            }
        }

    }
}