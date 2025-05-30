package request_handler

import aws.smithy.kotlin.runtime.http.HttpStatusCode
import com.amazonaws.HttpMethod
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import controller.BusController
import di.MainModule
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import model.request.BusCurrentNextStopUpdateRequest
import model.request.BusRegistrationRequest
import model.request.BusStatusUpdateRequest
import model.request.BusStopIdsUpdateRequest
import model.request.BusUpdateRequest
import model.response.BusControllerResponse
import model.response.HttpResponse
import model.response.generateHttpResponse
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import kotlin.jvm.Throws

class BusServiceHandler : RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent> {

    private val busController: BusController by inject(
        clazz = BusController::class.java
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
            context?.logger,
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

        logger?.log(body.toString())
        try {
            return@runBlocking when (resource) {

                "/buses" -> {

                    when (httpMethod) {
                        HttpMethod.POST.toString() -> toApiGatewayResponse {
                            body ?: return@runBlocking missingBodyError()
                            busController.registerBus(body.deserialize<BusRegistrationRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/buses/{id}" -> {

                    id ?: return@runBlocking missingIdError()

                    when (httpMethod) {
                        HttpMethod.GET.toString() -> toApiGatewayResponse { busController.getBus(id) }
                        HttpMethod.DELETE.toString() -> toApiGatewayResponse { busController.deleteBus(id) }
                        HttpMethod.PATCH.toString() -> toApiGatewayResponse {
                            body ?: return@runBlocking missingBodyError()
                            busController.updateBusDetails(id, body.deserialize<BusUpdateRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/buses/{id}/current-next-stop" -> {

                    id ?: return@runBlocking missingIdError()

                    when (httpMethod) {
                        HttpMethod.PATCH.toString() -> toApiGatewayResponse<Unit> {
                            body ?: return@runBlocking missingBodyError()
                            busController.updateCurrentAndNextStop(id, body.deserialize<BusCurrentNextStopUpdateRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/buses/{id}/status" -> {

                    id ?: return@runBlocking missingIdError()

                    when (httpMethod) {
                        HttpMethod.PATCH.toString() -> toApiGatewayResponse<Unit> {
                            body ?: return@runBlocking missingBodyError()
                            busController.updateBusStatus(id, body.deserialize<BusStatusUpdateRequest>())
                        }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/buses/batch" -> {
                    ids ?: return@runBlocking missingQueryError()
                    logger?.log(ids)

                    when (httpMethod) {
                        HttpMethod.GET.toString() -> toApiGatewayResponse { busController.getBuses(ids.split("&")) }
                        else -> methodNotAllowedResponse()
                    }
                }

                "/buses/stop-ids" -> {

                    when (httpMethod) {
                        HttpMethod.PATCH.toString() -> toApiGatewayResponse<Unit> {
                            body ?: return@runBlocking missingBodyError()
                            busController.updateStopIds(body.deserialize<BusStopIdsUpdateRequest>())
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
        controllerResponse: suspend () -> BusControllerResponse<T>,
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
        .withBody(MISSING_ID_ERROR_MESSAGE)
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
