import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.smithy.kotlin.runtime.ExperimentalApi
import controller.BusController
import controller.BusControllerImpl
import data.db_converters.BusItemConverter
import data.entity.BusEntity
import data.model.Bus
import data.model.BusStatus
import data.respository.BusRepository
import data.source.DynamoDbDataSource
import data.util.BusEntityAttrUpdate
import data.util.BusEntityAttrUpdate.UpdateStopIds.Companion.StopIdsUpdateAction
import data.util.GetBack
import di.MainModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import model.BusStatusDto
import model.request.BusRegistrationOrUpdateRequest
import model.request.BusStatusUpdateRequest
import model.request.BusStopIdsUpdateRequest
import model.response.BusControllerResponse
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.security.MessageDigest
import kotlin.math.abs

//
//@OptIn(ExperimentalStdlibApi::class)
//fun main(){
//
////    val id1 = "67YUHGUJB"
////    val id2  = "73BJBKKK"
////
////    fun String.hashWithSha256(): String =
////        MessageDigest.getInstance("SHA-256")
////            .digest(toByteArray())
////            .toHexString()
////
////    val hash1 = id1.hashWithSha256()
////    val hash2 = id2.hashWithSha256()
////
////    println("id1: ${hash1.toLong(16).toInt()} ")
////    println("id2: ${hash2.toLong(16).toInt()} ")
////
////    fun testt(){
////        val a = "ABUK090"
////       // println(abs(a))
////
////    }
////    testt()
//    ///////
//}


suspend fun main(){
    startKoin {
        modules(MainModule)
    }

    val source : DynamoDbDataSource<BusEntity> by inject(
        clazz = DynamoDbDataSource::class.java,
    )

    val repo : BusRepository by inject(
        clazz = BusRepository::class.java
    )

    val controller : BusController by inject(
        clazz = BusController::class.java
    )
    val busRequests = listOf(
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0006",
            driverName = "JohnDoe",
            activeHours = "06:00-18:00",
            activeDays = "Monday-Friday",
            busStatus = BusStatusDto.InActive
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0007",
            driverName = "JaneSmith",
            activeHours = "07:00-19:00",
            activeDays = "Monday-Saturday",
            busStatus = BusStatusDto.InMaintenance
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0008",
            driverName = "BobJohnson",
            activeHours = "08:00-20:00",
            activeDays = "Monday-Sunday",
            busStatus = BusStatusDto.InActive
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0009",
            driverName = "AliceBrown",
            activeHours = "09:00-21:00",
            activeDays = "Tuesday-Sunday",
            busStatus = BusStatusDto.Active
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0010",
            driverName = "MikeDavis",
            activeHours = "10:00-22:00",
            activeDays = "Wednesday-Sunday",
            busStatus = BusStatusDto.Active
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-1111",
            driverName = "MikeDavis",
            activeHours = "10:00-22:00",
            activeDays = "Wednesday-Sunday",
            busStatus = BusStatusDto.Active
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0089",
            driverName = "MikeDavis",
            activeHours = "10:00-22:00",
            activeDays = "Wednesday-Sunday",
            busStatus = BusStatusDto.Active
        ),
        BusRegistrationOrUpdateRequest(
            busId = "BUS-0090",
            driverName = "MikeDavis",
            activeHours = "10:00-22:00",
            activeDays = "Wednesday-Sunday",
            busStatus = BusStatusDto.Active
        )
    )

    fun <T> eval(busNo:Int,result: BusControllerResponse<T>){
        when(result){
            is BusControllerResponse.Error -> println("$busNo --- Error --- ${result.error.message}")
            is BusControllerResponse.Success<*> -> println("$busNo --- Success ---${result.data}")
        }
    }

    runBlocking {
//        eval<Nothing>(0, controller.registerBus(busRequests[0]))
        eval<Nothing>(1, controller.registerBus(busRequests[1]))
//        eval<Nothing>(2, controller.registerBus(busRequests[2]))
//        eval<Nothing>(3, controller.registerBus(busRequests[3]))
//        eval<Nothing>(4, controller.registerBus(busRequests[4]))
//        eval<Nothing>(5, controller.registerBus(busRequests[5]))
       //   eval<Nothing>(8, controller.registerBus(busRequests[6]))
//        eval<Nothing>(7, controller.updateStopIds(BusStopIdsUpdateRequest(
//            busId = "BUS-0090",
//            stopIds = listOf("stop1", "stop2"),
//            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
//        )))
        eval<Nothing>(13, controller.updateStopIds(BusStopIdsUpdateRequest(
            busId = "BUS-0089",
            stopIds = listOf("stop1", "stop2"),
            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
        )))
        eval<Nothing>(14, controller.updateStopIds(BusStopIdsUpdateRequest(
            busId ="BUS-0009",
            stopIds = listOf("stop1", "stop2"),
            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
        )))
        eval<Nothing>(15, controller.updateStopIds(BusStopIdsUpdateRequest(
            busId ="BUS-0008",
            stopIds = listOf("stop1", "stop2"),
            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
        )))
        eval<Nothing>(16, controller.updateStopIds(BusStopIdsUpdateRequest(
            busId ="BUS-0007",
            stopIds = listOf("stop1", "stop2"),
            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Add
        )))
//        eval<Nothing>(17, controller.updateStopIds(BusStopIdsUpdateRequest(
//            busId ="BUS-0007",
//            stopIds = listOf("stop1", "stop2"),
//            updateType = BusStopIdsUpdateRequest.Companion.UpdateType.Remove
//        )))

//        eval(9, controller.updateBusStatus(BusStatusUpdateRequest( "BUS-0089", BusStatusDto.InActive)))
        eval(10,controller.getBus("BUS-0089"))
        //eval<Nothing>(11, controller.registerBus(busRequests[7]))
        eval(12,controller.getBus("BUS-0090"))
        eval(12,controller.getBus("BUS-0009"))
        eval(12,controller.getBus("BUS-0007"))
        eval(18,controller.getBus("BUS-0000"))

        eval(19,controller.deleteBus("BUS-0000"))
      //  eval(20,controller.deleteBus("BUS-0007"))

        eval(21, controller.updateBusStatus(BusStatusUpdateRequest( "BUS-0007", BusStatusDto.InActive)))
        //eval(23,controller.deleteBus("BUS-0007"))
        delay(1000)
        eval(22, controller.updateBusDetails(
            BusRegistrationOrUpdateRequest(
                busId = "BUS-0007",
                driverName = busRequests[1].driverName,
                activeHours = busRequests[1].activeHours,
                activeDays = busRequests[1].activeDays,
                busStatus = BusStatusDto.InActive
            ))
        )
        eval(24,controller.getBus("BUS-0007"))

        eval(25,controller.getBuses(listOf("BUS-0008", "BUS-0007", "BUS-0006")))

    }

//    source.updateItemAttr(
//            // updateAction =  DynamoDbUpdateAttrActionType.Add,
//            update =  BusEntityAttrUpdate.UpdateStopIds(
//                value = listOf("stop1","stop2","stop3","stop4"),
//                updateAction = StopIdsUpdateAction.Add
//            ),
//            "BUS-0089"
//        ).also {
//            when(it){
//                is GetBack.Error -> {println("Failed ........"+ it.toString())}
//                is GetBack.Success -> { println("Sucess......")
//                    println(it.toString())
//                }
//            }
//        }


}

//@OptIn(ExperimentalApi::class)
//suspend fun main(){
//
//    startKoin {
//        modules(MainModule)
//    }
//    val q  = QueryRequest{
//
//    }
//    val converter = BusItemConverter()
//
//
//    val source : DynamoDbDataSource<BusEntity> by inject(
//        clazz = DynamoDbDataSource::class.java,
//    )
//    val repo : BusRepository by inject(
//        clazz = BusRepository::class.java
//    )
//
//    val m = mapOf("bb" to "KWNDCE")
//    //mapper.get
//
//    println(
//       "Map here " + m["aa"]
//    )
//    val demData = BusEntity(
//        busId = "9237tgg4aswwwasdfdffdffddfsaaskopkokkoasas9qqq0124",
//        partitionKey = "sklmxd",
//        driverName = "Test Driver",
//        activeDays = "all day",
//        activeHours = "all hour",
//        busStatus =  null,
//        stopIds = listOf("stop1", "stop2"),
//        currentStop = "wddqwdqdq",
//        nextStop = null,
//    )
//
////    runBlocking {
////        source.putItem(demData).also {
////            when (it) {
////                is GetBack.Error -> {
////                    println("Failed ........")
////                }
////
////                is GetBack.Success -> {
////                    println("Sucess......")
////                }
////            }
////        }
//////
//////        repo.registerBus(demData).also {
//////            when(it){
//////                is GetBack.Error -> {println("from repo error 0 ${it.message} ........")}
//////                is GetBack.Success -> { println("from repo 0 ${it.data} ........")}
//////            }
//////        }
////
////        repo.fetchBusByBusId(demData.busId).also {
////            when(it){
////                is GetBack.Error -> {println("from repo error 1 ${it.message} ........")}
////                is GetBack.Success -> { println("from repo 1 ${it.data} ........")}
////            }
////        }
////
////        repo.updateNextStop(busId = demData.busId, nextBusStopId = "eddewed").also {
////            when(it){
////                is GetBack.Error -> {println("from repo error 2 ${it.message} ........")}
////                is GetBack.Success -> { println("from repo 2${it.data} ........")}
////            }
////        }
////        source.getItemsInBatch(
////            listOf("9237492411","33")
////        ).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........")}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////        source.getItem((92374924).toString()).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........")}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////        source.deleteItem((92374924).toString()).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........"+it.toString())}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////        repo.fetchBusByBusId(demData.busId).also {
////            when(it){
////                is GetBack.Error -> {println("from repo error 3 ${it.message} ........")}
////                is GetBack.Success -> { println("from repo 3${it.data} ........")}
////            }
////        }
////
////        source.getItem((92374924).toString()).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........"+it.toString())}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////        source.deleteItem((923724).toString()).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........")}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////
//////        source.putItem(demData).also {
//////            when(it){
//////                is GetBack.Error -> {println("Failed ........")}
//////                is GetBack.Success -> { println("Sucess......")}
//////            }
//////        }
////        source.updateItemAttr(
////            // updateAction =  DynamoDbUpdateAttrActionType.Add,
////            update =  BusEntityAttrUpdate.UpdateStopIds(
////                value = listOf("stop1","stop2","stop3","stop4"),
////                updateAction = StopIdsUpdateAction.Add
////            ),
////            "9237492411"
////        ).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........"+ it.toString())}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////
////        source.getItem((92374924).toString()).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........")}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////
//////        source.updateItemAttr(
//////            update =  BusEntityAttrUpdate.UpdateStopIds(
//////                listOf("stop3"),
//////                StopIdsUpdateAction.Delete
//////            ),
//////            "92374924"
//////        ).also {
//////            when(it){
//////                is GetBack.Error -> {println("Failed ........"+it.toString())}
//////                is GetBack.Success -> { println("Sucess......")
//////                    println(it.toString())
//////                }
//////            }
//////        }
//////
////        source.updateItemAttr(
////            update =  BusEntityAttrUpdate.UpdateBusStatus(
////                BusStatus.InMaintenance,
////            ),
////            "9237492411"
////        ).also {
////            when(it){
////                is GetBack.Error -> {println("Failed updatinggg ........"+it.toString())}
////                is GetBack.Success -> { println("Sucess updatinggggggg......")
////                    println(it.toString())
////                }
////            }
////        }
////
////        source.updateItemAttr(
////            update =  BusEntityAttrUpdate.UpdateNextStop(
////                "dqm;lqdwqdwqdw"
////            ),
////            "9237492411"
////        ).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........"+it.toString())}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////
////        source.updateItemAttr(
////            update =  BusEntityAttrUpdate.UpdateCurrentStop(
////                "dqm;lqdwqdwqdw"
////            ),
////            "9237492411"
////        ).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........"+it.toString())}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
////
////        source.getItem((92374924).toString()).also {
////            when(it){
////                is GetBack.Error -> {println("Failed ........")}
////                is GetBack.Success -> { println("Sucess......")
////                    println(it.toString())
////                }
////            }
////        }
//////        source.transactWriteItems(
//////            listOf(
//////                DynamoDbTransactWriteItem(
//////                    putItem = demData,
//////                    deleteItemKey = null
//////                ),
//////                DynamoDbTransactWriteItem(
//////                    putItem = demData2,
//////                    deleteItemKey = null
//////                )
//////            )
//////        ).also {
//////            when(it){
//////                is GetBack.Error -> {println("Failed ........")}
//////                is GetBack.Success -> { println("Sucess......")
//////                    println(it.toString())
//////                }
//////            }
//////        }
////    }
//
////    val serData = converter.convertFrom(converter.convertTo(demData))
////    println(
////        converter.convertTo(demData)
////    )
////
//}