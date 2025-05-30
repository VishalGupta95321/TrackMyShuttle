package request_handler

import aws.smithy.kotlin.runtime.http.HttpStatusCode
import com.amazonaws.HttpMethod
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import controller.BusStopController
import di.MainModule
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import model.request.AddBusStopRequest
import model.request.BatchAddBusStopRequest
import model.request.UpdateBusIdsInStopsRequest
import model.request.UpdateBusStopRequest
import model.response.BusStopControllerResponse
import model.response.HttpResponse
import model.response.generateHttpResponse
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

class BusStopHandler : RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent> {

    private val busStopController: BusStopController by inject(
        clazz = BusStopController::class.java
    )

    private val json: Json by inject(
        clazz = Json::class.java
    )

    override fun handleRequest(
        input: APIGatewayProxyRequestEvent?,
        context: Context?
    ): APIGatewayProxyResponseEvent {

        if (input == null)
            return APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.BadRequest.value)
                .withBody(INVALID_BODY_MESSAGE)

        val response = handleRoutes(
            input.resource,
            input.pathParameters,
            input.queryStringParameters,
            input.httpMethod,
            input.body,
            context?.logger
        )

        return response
    }

    private fun handleRoutes(
        resource: String,
        pathParameter: Map<String, String>?,
        queryParameters: Map<String, String>?,
        httpMethod: String,
        body: String?,
        logger: LambdaLogger?
    ): APIGatewayProxyResponseEvent = runBlocking {


        val id = pathParameter?.get("id")
        val ids = queryParameters?.get("ids")
        val subString = queryParameters?.get("address_substring")
        val fromStop = queryParameters?.get("fromStop")
        val toStop = queryParameters?.get("toStop")


        try {
            return@runBlocking when (resource) {
                // Get handled by Step Function
                "/bus_stops" -> {
                    when (httpMethod) {

                        HttpMethod.GET.toString() -> {
                            subString ?: return@runBlocking missingQueryError()
                            toApiGatewayResponse { busStopController.getBusStopsByAddressSubstring(subString) }
                        }
                        HttpMethod.POST.toString() -> toApiGatewayResponse {
                            logger?.log(body)
                            body ?: return@runBlocking missingBodyError()
                            busStopController.addBusStop(body.deserialize<AddBusStopRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/bus_stops/{id}" -> {

                    id ?: return@runBlocking missingIdError()

                    when (httpMethod) {
                        HttpMethod.GET.toString() -> toApiGatewayResponse { busStopController.getBusStop(id) }
                        HttpMethod.DELETE.toString() -> toApiGatewayResponse { busStopController.deleteBusStop(id) }
                        HttpMethod.PATCH.toString() -> toApiGatewayResponse {
                            body ?: return@runBlocking missingBodyError()
                            busStopController.updateBusStop(id, body.deserialize<UpdateBusStopRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/bus_stops/batch" -> {


                    when (httpMethod) {

                        HttpMethod.DELETE.toString() -> {
                            ids ?: return@runBlocking missingQueryError()

                            toApiGatewayResponse { busStopController.batchDeleteBusStop(ids.split("&")) }
                        }

                        HttpMethod.GET.toString() -> {
                            ids ?: return@runBlocking missingQueryError()

                            toApiGatewayResponse { busStopController.getBusStops(ids.split("&")) }
                        }

                        HttpMethod.POST.toString() -> toApiGatewayResponse {
                            body ?: return@runBlocking missingBodyError()
                            busStopController.batchAddBusStop(body.deserialize<BatchAddBusStopRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/bus_stops/batch/bus-ids" -> {


                    when (httpMethod) {
                        HttpMethod.PATCH.toString() -> toApiGatewayResponse<Unit> {
                            body ?: return@runBlocking missingBodyError()
                            busStopController.updateBusIdsInStops(body.deserialize<UpdateBusIdsInStopsRequest>())
                        }
                        HttpMethod.GET.toString() -> {
                            fromStop ?: return@runBlocking missingQueryError()
                            toStop ?: return@runBlocking missingQueryError()
                            toApiGatewayResponse { busStopController.getBusIdsByStops(fromStop,toStop) }
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                else -> methodNotAllowedResponse()
            }
        } catch (e: SerializationException) {
            logger?.log(e.toString())
            return@runBlocking invalidBodyError()
        } catch (e: Exception) {
            logger?.log(e.toString())
            return@runBlocking internalServerError()
        }
    }


    private suspend inline fun <reified T> toApiGatewayResponse(
        controllerResponse: suspend () -> BusStopControllerResponse<T>,
    ): APIGatewayProxyResponseEvent {
        val controllerResponse = controllerResponse()
        val httpResponse = controllerResponse.generateHttpResponse()

        return APIGatewayProxyResponseEvent().apply {
            withStatusCode(httpResponse.code.value)
            httpResponse.body?.let {
                withBody(json.encodeToString(httpResponse.body))
            }
            withIsBase64Encoded(false)
        }

    }

    @Throws(SerializationException::class)
    private inline fun <reified T : Any> String.deserialize(): T {
        return json.decodeFromString<T>(this)
    }


    private fun methodNotAllowedResponse() = APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.MethodNotAllowed.value)
        .withBody(METHOD_NOT_ALLOWED_ERROR_MESSAGE)
        .withIsBase64Encoded(false)

    private fun missingQueryError() = APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.BadRequest.value)
        .withBody(MISSING_QUERY_PARAM_ERROR_MESSAGE)
        .withIsBase64Encoded(false)


    private fun missingIdError() = APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.BadRequest.value)
        .withBody(Companion.MISSING_ID_ERROR_MESSAGE)
        .withIsBase64Encoded(false)

    private fun invalidBodyError() = APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.BadRequest.value)
        .withBody(INVALID_BODY_MESSAGE)
        .withIsBase64Encoded(false)

    private fun internalServerError() = APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.InternalServerError.value)
        .withBody(UNDEFINED_ERROR_MESSAGE)
        .withIsBase64Encoded(false)

    private fun missingBodyError() = APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.BadRequest.value)
        .withBody(MISSING_BODY_ERROR_MESSAGE)
        .withIsBase64Encoded(false)

    companion object {
        const val METHOD_NOT_ALLOWED_ERROR_MESSAGE = "Method not allowed"
        const val MISSING_QUERY_PARAM_ERROR_MESSAGE = "Missing query parameters"
        const val MISSING_ID_ERROR_MESSAGE = "Missing Id"
        const val INVALID_BODY_MESSAGE = "Invalid body"
        const val UNDEFINED_ERROR_MESSAGE = "Undefined error"
        const val MISSING_BODY_ERROR_MESSAGE = "Missing body"


    }

    init {
        startKoin {
            modules(MainModule)
        }
    }
}
