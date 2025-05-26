import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import controller.BusStopController
import data.entity.BusStopEntity
import data.model.BusStopScanned
import data.source.DynamoDbDataSource
import di.MainModule
import kotlinx.coroutines.runBlocking
import model.LocationDto
import model.request.AddBusStopRequest
import model.request.GetBusStopsByAddressSubstringRequest
import model.request.UpdateBusStopRequest
import model.response.BusStopControllerResponse
import model.response.BusStopDto
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject


const val INDEX = "BUS_STOP_ADDRESS_INDEX"

fun demoData() =  listOf(
    AddBusStopRequest(
        listOf(
            BusStopDto(
                stopID = "BS001",
                stopName = "Main Street Stop",
                address = "123 Main St",
                location = LocationDto("37.7749", "-122.4194")
            )
        )
    ),
      AddBusStopRequest(
        listOf(
            BusStopDto(
                stopID = "BS002",
                stopName = "Park Avenue Stop",
                address = "456 Park Ave",
                location = LocationDto("40.7128", "-74.0060")
            ),
            BusStopDto(
                stopID = "BS004",
                stopName = "Central Station Stop",
                address = "901 Central Station",
                location = LocationDto("51.5074", "-0.1278")
            )
        )
    ),
    AddBusStopRequest(
        listOf(
            BusStopDto(
                stopID = "BS005",
                stopName = "Downtown Stop",
                address = "234 Downtown Dr",
                location = LocationDto("29.7604", "-95.3698")
            ),
            BusStopDto(
                stopID = "BS006",
                stopName = "Uptown Stop",
                address = "567 Uptown Blvd",
                location = LocationDto("30.2672", "-97.7431")
            )
        )
    )
)



suspend fun queryRequest(db: DynamoDbClient){
    val queryRequest = QueryRequest {
        tableName = "BUS_TABLE"
        indexName = "TestGSIIndex"
        keyConditionExpression = "begins_with(#pk, :pkPrefix)"
        expressionAttributeNames = mapOf("#pk" to "driverName")
        expressionAttributeValues = mapOf(":pkPrefix" to AttributeValue.S("J"))
    }

    val queryRequest2 = QueryRequest {
        tableName = "BUS_TABLE"
        indexName = "TestGSIIndex"
        filterExpression = ""
        keyConditionExpression = "#dn = :dnValue"
        expressionAttributeNames = mapOf("#dn" to "driverName")
        expressionAttributeValues = mapOf(":dnValue" to AttributeValue.S("J"))
    }

    val scanRequest = ScanRequest {
        tableName = "BUS_STOP_TABLE"
        indexName = INDEX
        filterExpression = "contains(#pk, :pkPrefix)"
        expressionAttributeNames = mapOf("#pk" to "address")
        expressionAttributeValues = mapOf(":pkPrefix" to AttributeValue.S("St"))
    }

    //print(db.query(queryRequest2).items)
    print(db.scan(scanRequest).items)

}

fun main (){

    startKoin {
        modules(MainModule)
    }

    val db : DynamoDbClient by inject(clazz = DynamoDbClient::class.java)

    val source : DynamoDbDataSource<BusStopEntity, BusStopScanned> by inject(
        clazz = DynamoDbDataSource::class.java,
    )

    val controller : BusStopController by inject(
        clazz = BusStopController::class.java
    )

    fun <T> eval(busNo:Int,result: BusStopControllerResponse<T>){
        when(result){
            is BusStopControllerResponse.Error -> println("$busNo --- Error --- ${result.error.message}")
            is BusStopControllerResponse.Success<*> -> println("$busNo --- Success ---${result.data}")
        }
    }

    runBlocking {
     //   eval(1, controller.addBusStop(demoData()[0]))
       ///// eval(2, controller.getBusStop("BS001"))
       ///// eval(3, controller.getBusStops(listOf("BS001","BS002")))
       //  eval(4, controller.addBusStop(demoData()[1]))
       // eval(5, controller.addBusStop(demoData()[2]))
//        eval(6, controller.getBusStops(listOf("BS002","BS004")))
//        eval(7, controller.getBusStops(listOf("BS005","BS006")))
//        eval(8, controller.deleteBusStop(listOf("BS005","BS006")))
//        eval(9, controller.getBusStops(listOf("BS005","BS006")))
//        eval(10, controller.updateBusStop(UpdateBusStopRequest( BusStopDto(
//            stopID = "BS001",
//            stopName = "Uptown Stop",
//            address = "567 Uptown Blvd",
//            location = LocationDto("30.2672", "-97.7431")
//        ))))
//        delay(1000)
//        eval(11, controller.getBusStop("BS001"))
       ////// eval(12, controller.getBusStop("BS006"))
        //queryRequest(db)
        eval(1, controller.addBusStop(demoData()[0]))
        eval(10, controller.updateBusStop(UpdateBusStopRequest( BusStopDto(
            stopID = "BS0rdddrr89",
            stopName = "Uptown Stop",
            address = "567 Uptown Blvd",
            location = LocationDto("30.2672", "-97.7431")
        ))))
        eval(8, controller.deleteBusStop(listOf("BS","hkxbwa")))
        eval(13, controller.getBusStopsByAddressSubstring(GetBusStopsByAddressSubstringRequest("123")))
        eval(14, controller.getBusStops(listOf("BS005","BS006","texpkkasda")))
    }
}






















suspend fun describeTable(db: DynamoDbClient){
    val describeTableRequest = DescribeTableRequest {
        tableName = "BUS_STOP_TABLE"
    }

    val response = db.describeTable(describeTableRequest)
    println(response.table)
}

suspend fun createGlobalSecondaryIndex(db: DynamoDbClient){

    val updateTableRequest = UpdateTableRequest {
        tableName = "BUS_STOP_TABLE"
        attributeDefinitions = listOf(
            AttributeDefinition {
                attributeName = "stopId"
                attributeType = ScalarAttributeType.S
            },
            AttributeDefinition {
                attributeName = "stopName"
                attributeType = ScalarAttributeType.S
            },
            AttributeDefinition {
                attributeName = "address"
                attributeType = ScalarAttributeType.S
            }
        )
        globalSecondaryIndexUpdates = listOf(
            GlobalSecondaryIndexUpdate{
                create = CreateGlobalSecondaryIndexAction{
                    indexName = "BUS_STOP_ADDRESS_INDEX"
                    keySchema = listOf(
                        KeySchemaElement{
                            attributeName = "stopName"
                            keyType = KeyType.Hash
                        },
                        KeySchemaElement{
                            attributeName = "address"
                            keyType = KeyType.Range
                        }
                    )
                    projection = Projection{
                        projectionType = ProjectionType.KeysOnly
                    }
                    provisionedThroughput = ProvisionedThroughput{
                        readCapacityUnits = 10
                        writeCapacityUnits = 10
                    }
                }
            }
        )
    }

    db.updateTable(updateTableRequest)
}

