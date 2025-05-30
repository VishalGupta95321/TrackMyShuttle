package model.response

import aws.smithy.kotlin.runtime.http.HttpStatusCode
import exceptions.BusControllerExceptions

data class HttpResponse<T>(
    val code: HttpStatusCode,
    val body: T? = null,
)


fun <T> BusControllerResponse<T>.generateHttpResponse(): HttpResponse<T> {
    return when(this){
        is BusControllerResponse.Success -> {
            if (this.data!=null)
                HttpResponse(code = HttpStatusCode.OK, body = this.data)
            else HttpResponse(code = HttpStatusCode.NoContent)
        }
        is BusControllerResponse.Error ->  {
            when(this.error){
                BusControllerExceptions.SomethingWentWrong -> HttpResponse(code = HttpStatusCode.InternalServerError)
                BusControllerExceptions.InvalidInput -> HttpResponse(code = HttpStatusCode.BadRequest)
                BusControllerExceptions.ItemAlreadyExists -> HttpResponse(code = HttpStatusCode.Conflict)
                BusControllerExceptions.ItemNotFound -> HttpResponse(code = HttpStatusCode.NotFound)
                BusControllerExceptions.RegistrationError -> HttpResponse(code = HttpStatusCode.InternalServerError)
            }
        }
    }
}