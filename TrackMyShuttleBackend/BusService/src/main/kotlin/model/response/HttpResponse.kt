package model.response

import exceptions.BusControllerExceptions

data class HttpResponse<T>(
    val code: String,
    val body: T? = null,
)

// TODO("Later use the appropriate http error code but for now lets stick with just these three")
object HttpStatusCode{
    const val OK = "200"
    const val BAD_REQUEST = "400"
    const val INTERNAL_SERVER_ERROR = "500"
}


fun <T> generateHttpResponse(response: BusControllerResponse<T>): HttpResponse<T> {
    return when(response){
        is BusControllerResponse.Success -> HttpResponse(code = HttpStatusCode.OK, body = response.data)
        is BusControllerResponse.Error ->  {
            when(response.error){
                BusControllerExceptions.SomethingWentWrong -> HttpResponse(code = HttpStatusCode.INTERNAL_SERVER_ERROR)
                BusControllerExceptions.InvalidInput -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                BusControllerExceptions.ItemAlreadyExists -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                BusControllerExceptions.ItemNotFound -> HttpResponse(code = HttpStatusCode.BAD_REQUEST)
                BusControllerExceptions.RegistrationError -> HttpResponse(code = HttpStatusCode.INTERNAL_SERVER_ERROR)
            }
        }
    }
}