package request_handler


import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import data.entity.BusEntity
import data.entity.BusEntityAttributes
import data.source.DynamoDbDataSource
import data.util.GetBack
import di.MainModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

//class SimpleHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
//
//    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
//        // Log the incoming request
//        context.logger.log("Received event: ${input.body}")
//
//        // Example: Access query parameters
//        val name = input.queryStringParameters?.get("name") ?: "Unknown"
//
//        // Create the response object
//        val response = APIGatewayProxyResponseEvent()
//            .withStatusCode(200)
//            .withBody("Hello, $name! Your request was successfully processed.")
//
//
//        return response
//    }
//}


//class SimpleHandler : RequestHandler<BusDataEntity, String> {
//
//
//    val source : DynamoDbDataSource<BusDataEntity> by inject(
//        clazz = DynamoDbDataSource::class.java,
//        qualifier = named("BusDataEntity")
//    )
//
//    override fun handleRequest(input: BusDataEntity, context: Context): String {
//        val logger = context.logger
//        logger.log("Input received: $input")
//
//        runBlocking {
//            source.putItem(input).also {
//                when (it) {
//                    is GetBack.Error -> {
//                        println("Failed ........")
//                    }
//
//                    is GetBack.Success -> {
//                        println("Sucess......")
//                    }
//                }
//            }
//        }
//        // Generate a response
//        return "Hello, Lambda! Received input: $input"
//    }
//
//    init {
//        startKoin {
//            modules(MainModule)
//        }
//    }
//}


//class SimpleHandler : RequestHandler<BusEntity, String> {
//
//    val source : DynamoDbDataSource<BusEntity> by inject(
//        clazz = DynamoDbDataSource::class.java,
//    )
//
//    val client: DynamoDbClient by inject(clazz = DynamoDbClient::class.java)
//
//    override fun handleRequest(input: BusEntity, context: Context): String {
//        val logger = context.logger
//        logger.log("Input received: ${input.stopIds}")
//
//        val demData = BusEntity(
//            busId = "92374924",
//            driverName = "Test Driver",
//            activeDays = "all day",
//            activeHours = "all hour",
//            busStatus =  null,
//            stopIds = listOf("stop1", "stop2"),
//            currentStop = "wddqwdqdq",
//            nextStop = null,
//        )
//        val demData1 = BusEntity(
//            busId = "9237493324",
//            driverName = "1Test Driver ",
//            activeDays = "all day",
//            activeHours = "all hour",
//            busStatus =  null,
//            stopIds = listOf("stop1", "stop2"),
//            currentStop = "wddqwdqdq",
//            nextStop = null,
//        )
//        val demData2 = BusEntity(
//            busId = "92344474924",
//            driverName = "2Test Driver",
//            activeDays = "all day",
//            activeHours = "all hour",
//            busStatus =  null,
//            stopIds = listOf("stop1", "stop2"),
//            currentStop = "wddqwdqdq",
//            nextStop = null,
//        )
//
//        runBlocking {
//            source.putItem(demData).also {
//                when(it){
//                    is GetBack.Error -> {println("Failed ........")}
//                    is GetBack.Success -> { println("Sucess......")}
//                }
//            }
//            source.putItem(demData1).also {
//                when(it){
//                    is GetBack.Error -> {println("Failed ........")}
//                    is GetBack.Success -> { println("Sucess......")}
//                }
//            }
//            source.putItem(demData2).also {
//                when(it){
//                    is GetBack.Error -> {println("Failed ........")}
//                    is GetBack.Success -> { println("Sucess......")}
//                }
//            }
//        }
//
//        // Generate a response
//        return "Hello, Lambda! Received input: $input"
//    }
//
//   init {
//       startKoin {
//           modules(MainModule)
//       }
//   }
//}




//// getting data from both
class SimpleHandler : RequestHandler<Map<String, Any>, Map<String, Any>> {

    override fun handleRequest(input: Map<String, Any>, context: Context): Map<String, Any> {
        // Log the incoming request
        context.logger.log("Received event: ${input}")

        // Example: Access query parameters
        val name = input["name"] ?: "Unknown"

        // Create the response object
        val response = APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withBody("Hello, $name! Your request was successfully processed.")


       // return response
        return mapOf(
           // "statusCode" to 200,
            "headers" to mapOf("Content-Type" to "application/json"),
            "body" to """{"message": "Processed API Gateway request successfully!"}"""
        )
    }
}



/// paging using step function
/// update model verification
/// update model in project
/// update bus stop model in here
/// receiving request from both step function and api gateway /// done its working
/// handle time out and other criteria in lambda function in api gateway
/// you also need to set the retry strategy in step function states like error equal,intervalsec,backoffrate etc
/// etc.

/// check sync and async