package model.response

import aws.smithy.kotlin.runtime.http.HttpStatusCode
import exceptions.BusStopControllerExceptions

data class HttpResponse<T>(
    val code: HttpStatusCode,
    val body: T? = null,
)


fun <T> BusStopControllerResponse<T>.generateHttpResponse(): HttpResponse<T> {
    return when(this){
        is BusStopControllerResponse.Success -> {
            if (this.data!=null)
                HttpResponse(code = HttpStatusCode.OK, body = this.data)
            else HttpResponse(code = HttpStatusCode.NoContent)
        }
        is BusStopControllerResponse.Error ->  {
            when(this.error){
                BusStopControllerExceptions.SomethingWentWrong -> HttpResponse(code = HttpStatusCode.InternalServerError)
                BusStopControllerExceptions.InvalidInput -> HttpResponse(code = HttpStatusCode.BadRequest)
                BusStopControllerExceptions.ItemAlreadyExists -> HttpResponse(code = HttpStatusCode.Conflict)
                BusStopControllerExceptions.ItemNotFound -> HttpResponse(code = HttpStatusCode.NotFound)
            }
        }
    }
}