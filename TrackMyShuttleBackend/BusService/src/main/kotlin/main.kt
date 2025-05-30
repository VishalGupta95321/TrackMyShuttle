import data.model.Bus
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import model.request.BusStopIdsUpdateRequest
import model.request.BusStopIdsUpdateRequest.Companion.UpdateType


//import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
//import aws.sdk.kotlin.services.dynamodb.model.*
//import com.google.gson.Gson
//import controller.BusController
//import data.entity.BusEntity
//import data.respository.BusRepository
//import data.source.DynamoDbDataSource
//import di.MainModule
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.runBlocking
//import model.BusStatusDto
//import model.request.BusRegistrationOrUpdateRequest
//import model.request.BusStatusUpdateRequest
//import model.request.BusStopIdsUpdateRequest
//import model.response.BusControllerResponse
//import org.koin.core.context.GlobalContext.startKoin
//import org.koin.java.KoinJavaComponent.inject
//
//
///// PROVISION_THROUGHPUT on both the GSI and Table should be the same.
///// GSI would be effective on both Query and scan operations.
///// We can choose to add non-key attributes in the GSI if needed.
///// Query operation always requires Partition Key but scan does not
///// Using GSI will only return the fields which you have provided while creation the index. Further if can use ProjectExpression to Filter more.
///// **** Transact write item can work with different tables So use that later when you need to updated stops in both Bus and Route Table.
///// for the requests just require like stop id , in that case just add query parameter
//
//
///////// correct Error thing in all.
//
//
//
////fun main(){
////    startKoin {
////        modules(MainModule)
////    }
////
////    val db : DynamoDbClient by inject(clazz = DynamoDbClient::class.java)
////
////
////    suspend fun describeTable(){
////        val describeTableRequest = DescribeTableRequest {
////            tableName = "BUS_TABLE"
////        }
////
////        val response = db.describeTable(describeTableRequest)
////        println(response.table)
////    }
////
////
////    suspend fun createGlobalSecondaryIndex(){
////
//////        val globalSecondaryIndex = GlobalSecondaryIndex{
//////            indexName = "TestGSIIndex"
//////            keySchema = listOf(
//////                KeySchemaElement{
//////                    attributeName = "driverName"
//////                    keyType = KeyType.Hash
//////                }
//////            )
//////            projection = Projection{
//////                projectionType = ProjectionType.KeysOnly
//////            }
//////            provisionedThroughput = ProvisionedThroughput{
//////                readCapacityUnits = 10
//////                writeCapacityUnits = 10
//////            }
//////        }
////
////        val updateTableRequest = UpdateTableRequest {
////            tableName = "BUS_TABLE"
////            attributeDefinitions = listOf(
////                AttributeDefinition {
////                    attributeName = "busId"
////                    attributeType = ScalarAttributeType.S
////                },
////                AttributeDefinition {
////                    attributeName = "driverName"
////                    attributeType = ScalarAttributeType.S
////                }
////            )
////            globalSecondaryIndexUpdates = listOf(
////                GlobalSecondaryIndexUpdate{
////                    create = CreateGlobalSecondaryIndexAction{
////                        indexName = "TestGSIIndex"
////                        keySchema = listOf(
////                            KeySchemaElement{
////                                attributeName = "driverName"
////                                keyType = KeyType.Hash
////                            }
////                        )
////                        projection = Projection{
////                            projectionType = ProjectionType.KeysOnly
////                        }
////                        provisionedThroughput = ProvisionedThroughput{
////                            readCapacityUnits = 10
////                            writeCapacityUnits = 10
////                        }
////                    }
////                }
////            )
////        }
////
////        db.updateTable(updateTableRequest)
////    }
////
////
////    suspend fun queryRequest(){
////        val queryRequest = QueryRequest {
////            tableName = "BUS_TABLE"
////            indexName = "TestGSIIndex"
////            keyConditionExpression = "begins_with(#pk, :pkPrefix)"
////            expressionAttributeNames = mapOf("#pk" to "driverName")
////            expressionAttributeValues = mapOf(":pkPrefix" to AttributeValue.S("J"))
////        }
////
////        val queryRequest2 = QueryRequest {
////            tableName = "BUS_TABLE"
////            indexName = "TestGSIIndex"
////            filterExpression = ""
////            keyConditionExpression = "#dn = :dnValue"
////            expressionAttributeNames = mapOf("#dn" to "driverName")
////            expressionAttributeValues = mapOf(":dnValue" to AttributeValue.S("J"))
////        }
////
////        val scanRequest = ScanRequest {
////            tableName = "BUS_TABLE"
////            indexName = "TestGSIIndex"
////            filterExpression = "begins_with(#pk, :pkPrefix)"
////            expressionAttributeNames = mapOf("#pk" to "driverName")
////            expressionAttributeValues = mapOf(":pkPrefix" to AttributeValue.S("J"))
////        }
////
////        //print(db.query(queryRequest2).items)
////        print(db.scan(scanRequest).items)
////
////
////    }
////
////    runBlocking {
////       // createGlobalSecondaryIndex()
////        describeTable()
////        queryRequest()
////    }
////}
////
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////suspend fun main(){
////    startKoin {
////        modules(MainModule)
////    }
////
////    val data = BusStatusUpdateRequest("wddwd", BusStatusDto.InActive)
////    val a = Gson().toJson(data)
////    val b = Gson().fromJson(a, BusStatusUpdateRequest::class.java)
////    println(b)
////
////    val source : DynamoDbDataSource<BusEntity> by inject(
////        clazz = DynamoDbDataSource::class.java,
////    )
////
////    val repo : BusRepository by inject(
////        clazz = BusRepository::class.java
////    )
////    val controller : BusController by inject(
////        clazz = BusController::class.java
////    )
////    val busRequests = listOf(
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0006",
////            driverName = "JohnDoe",
////            activeHours = "06:00-18:00",
////            activeDays = "Monday-Friday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0007",
////            driverName = "JaneSmith",
////            activeHours = "07:00-19:00",
////            activeDays = "Monday-Saturday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0008",
////            driverName = "BobJohnson",
////            activeHours = "08:00-20:00",
////            activeDays = "Monday-Sunday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0009",
////            driverName = "AliceBrown",
////            activeHours = "09:00-21:00",
////            activeDays = "Tuesday-Sunday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0010",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-1111",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0089",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        ),
////        BusRegistrationOrUpdateRequest(
////            busId = "BUS-0090",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        ),
////
////    )
////
////    fun <T> eval(busNo:Int,result: BusControllerResponse<T>){
////        when(result){
////            is BusControllerResponse.Error -> println("$busNo --- Error --- ${result.error.message}")
////            is BusControllerResponse.Success<*> -> println("$busNo --- Success ---${result.data}")
////        }
////    }
////
////    runBlocking {
////        eval<Nothing>(0, controller.registerBus(busRequests[0]))
//       // eval<Nothing>(1, controller.registerBus(busRequests[1]))
////        eval<Nothing>(2, controller.registerBus(busRequests[2]))
////        eval<Nothing>(3, controller.registerBus(busRequests[3]))
////        eval<Nothing>(4, controller.registerBus(busRequests[4]))
////        eval<Nothing>(5, controller.registerBus(busRequests[5]))
//       //   eval<Nothing>(8, controller.registerBus(busRequests[6]))
////        eval<Nothing>(7, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId = "BUS-0090",
////            stopIds = listOf("stop1", "stop2"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
////        )))
////        eval<Nothing>(13, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId = "BUS-0089",
////            stopIds = listOf("stop1", "stop2"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
////        )))
////        eval<Nothing>(14, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId ="BUS-0009",
////            stopIds = listOf("stop1", "stop2"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
////        )))
////        eval<Nothing>(15, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId ="BUS-0008",
////            stopIds = listOf("stop1", "stop2"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
////        )))
////        eval<Nothing>(16, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId ="BUS-0007",
////            stopIds = listOf("stop1", "stop2"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
////        )))
////        eval<Nothing>(17, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId ="BUS-0007",
////            stopIds = listOf("stop1", "stop2"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Remove
////        )))
//
////        eval(9, controller.updateBusStatus(BusStatusUpdateRequest( "BUS-0089", BusStatusDto.InActive)))
////        eval(10,controller.getBus("BUS-0089"))
////        //eval<Nothing>(11, controller.registerBus(busRequests[7]))
////        eval(12,controller.getBus("BUS-0090"))
////        eval(12,controller.getBus("BUS-0009"))
////        eval(12,controller.getBus("BUS-0007"))
////        eval(18,controller.getBus("BUS-0000"))
////
////        eval(19,controller.deleteBus("BUS-0000"))
////      //  eval(20,controller.deleteBus("BUS-0007"))
////
////        eval(21, controller.updateBusStatus(BusStatusUpdateRequest( "BUS-0007", BusStatusDto.InActive)))
//        //eval(23,controller.deleteBus("BUS-0007"))
////        delay(1000)
////        eval(22, controller.updateBusDetails(
////            BusRegistrationOrUpdateRequest(
////                busId = "BUS-0007",
////                driverName = "XXXXXXXXXXX",
////                activeHours = busRequests[1].activeHours,
////                activeDays = busRequests[1].activeDays,
////            ))
////        )
////        eval(24,controller.getBus("BUS-0007"))
////
////        eval(25,controller.getBuses(listOf("BUS-0008", "BUS-0007", "BUS-0006")))
////        eval(26,controller.deleteBus("BUS-007898900"))
////        eval<Nothing>(27, controller.registerBus( BusRegistrationOrUpdateRequest(
////            busId = "BUS-0007",
////            driverName = "JaneSmith",
////            activeHours = "07:00-19:00",
////            activeDays = "Monday-Saturday",
////        )))
////        eval(28,controller.getBus("BUS-0007"))
////        eval<Nothing>(25, controller.registerBus(BusRegistrationOrUpdateRequest(
////            busId = "BUS-0099",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        )))
////        eval<Nothing>(25, controller.registerBus(BusRegistrationOrUpdateRequest(
////            busId = "BUS-0088",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        )))
////        eval<Nothing>(25, controller.registerBus(BusRegistrationOrUpdateRequest(
////            busId = "BUS-0077",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        )))
//     //   eval(29,controller.getBus("BUS-0077"))
////        eval<Nothing>(30, controller.registerBus(BusRegistrationOrUpdateRequest(
////            busId = "BUS-0066",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        )))
//    //    eval(31,controller.getBus("BUS-0066"))
////        eval<Nothing>(32, controller.registerBus(BusRegistrationOrUpdateRequest(
////            busId = "BUS-0055",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        )))
//        //eval(33,controller.getBus("BUS-0066"))
////        eval<Nothing>(34, controller.registerBus(BusRegistrationOrUpdateRequest(
////            busId = "BUS-0098",
////            driverName = "MikeDavis",
////            activeHours = "10:00-22:00",
////            activeDays = "Wednesday-Sunday",
////        )))
////        eval<Nothing>(35, controller.updateStopIds(BusStopIdsUpdateRequest(
////            busId = "BUS-0098",
////            stopIds = listOf("STOP8"),
////            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
////        )))
////        eval(36,controller.getBus("BUS-0098"))
//    }
//}


data class Test(
    val a: String,
    val b: String
)


@Throws(SerializationException::class)
private inline fun <reified T : Any> String.deserialize(): T {
    return Json.decodeFromString<T>(this)
}


fun main(){

    val json = Json

    val a = json.encodeToString(BusStopIdsUpdateRequest(
        "ddas",
        listOf("qwddw"),
        UpdateType.Add
    ))

    println(json.encodeToJsonElement(a))
    println(json.decodeFromString<BusStopIdsUpdateRequest>(a))

//    val request = BusStopIdsUpdateRequest(
//        stopIds = listOf("Stop1", "Stop2", "Stop3"),
//        updateType = UpdateType.Add,
//    )
    println(json.encodeToString("dwdwdw"))
//
    val l = listOf("Id1", "Id2", "Id3")
    val joined = l.joinToString("&")
    val split =joined.split("&")
    println(joined)
    println(split)
    val s = json.encodeToString(l)

    val j = json.decodeFromString<List<String>>(s)
    println(l)
    println(s)
    println(j)

    val encode  = Json.parseToJsonElement("{\"stopIds\":[\"Stop1\",\"Stop2\",\"Stop3\"],\"updateType\":{\"type\":\"UpdateTyype.Add\"}}").jsonObject
    println(encode)
}


