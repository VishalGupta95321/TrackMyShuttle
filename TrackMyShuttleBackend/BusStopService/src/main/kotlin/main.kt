import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import controller.BusStopController
import data.entity.BusStopEntity
import data.source.DynamoDbDataSource
import di.MainModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import model.LocationDto
import model.request.AddBusStopRequest
import model.request.UpdateBusStopRequest
import model.response.BusStopControllerResponse
import model.response.BusStopDto
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject

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

fun main (){

    startKoin {
        modules(MainModule)
    }

    val db : DynamoDbClient by inject(clazz = DynamoDbClient::class.java)

    val source : DynamoDbDataSource<BusStopEntity> by inject(
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
       // eval(1, controller.addBusStop(demoData()[0]))
        eval(2, controller.getBusStop("BS001"))
        eval(3, controller.getBusStops(listOf("BS001","BS002")))
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
        eval(12, controller.getBusStop("BS006"))


    }
}
