import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.db_converters.BusItemConverter
import data.entity.BusEntity
import data.model.BusStatus
import data.source.DynamoDbDataSource
import data.util.DynamoDbAttrUpdate
import data.util.DynamoDbAttrUpdate.BusDataAttrUpdate.UpdateStopIds.Companion.StopIdsUpdateAction
import data.util.GetBack
import di.MainModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject

@OptIn(ExperimentalApi::class)
suspend fun main(){

    startKoin {
        modules(MainModule)
    }
    val q  = QueryRequest{

    }
    val converter = BusItemConverter()

    val source : DynamoDbDataSource<BusEntity> by inject(
        clazz = DynamoDbDataSource::class.java,
    )


    val m = mapOf("bb" to "KWNDCE")
    //mapper.get

    println(
       "Map here " + m["aa"]
    )
    val demData = BusEntity(
        busId = "92374924",
        driverName = "Test Driver",
        activeDays = "all day",
        activeHours = "all hour",
        busStatus =  null,
        stopIds = listOf("stop1", "stop2"),
        currentStop = "wddqwdqdq",
        nextStop = null,
    )

    runBlocking {
        source.putItem(demData).also {
            when(it){
                is GetBack.Error -> {println("Failed ........")}
                is GetBack.Success -> { println("Sucess......")}
            }
        }
        source.getItemsInBatch(
            listOf("92374924","33")
        ).also {
            when(it){
                is GetBack.Error -> {println("Failed ........")}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }
        source.getItem((92374924).toString()).also {
            when(it){
                is GetBack.Error -> {println("Failed ........")}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }
        source.deleteItem((92374924).toString()).also {
            when(it){
                is GetBack.Error -> {println("Failed ........"+it.toString())}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }
        source.getItem((92374924).toString()).also {
            when(it){
                is GetBack.Error -> {println("Failed ........"+it.toString())}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }
        source.deleteItem((923724).toString()).also {
            when(it){
                is GetBack.Error -> {println("Failed ........")}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }

//        source.putItem(demData).also {
//            when(it){
//                is GetBack.Error -> {println("Failed ........")}
//                is GetBack.Success -> { println("Sucess......")}
//            }
//        }
        source.updateItemAttr(
            // updateAction =  DynamoDbUpdateAttrActionType.Add,
            update =  DynamoDbAttrUpdate.BusDataAttrUpdate.UpdateStopIds(
                value = listOf("stop1","stop2","stop3","stop4"),
                updateAction = StopIdsUpdateAction.Add
            ),
            "92374924"
        ).also {
            when(it){
                is GetBack.Error -> {println("Failed ........"+ it.toString())}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }

        source.getItem((92374924).toString()).also {
            when(it){
                is GetBack.Error -> {println("Failed ........")}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }

        source.updateItemAttr(
            update =  DynamoDbAttrUpdate.BusDataAttrUpdate.UpdateStopIds(
                listOf("stop3"),
                StopIdsUpdateAction.Delete
            ),
            "92374924"
        ).also {
            when(it){
                is GetBack.Error -> {println("Failed ........"+it.toString())}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }

        source.updateItemAttr(
            update =  DynamoDbAttrUpdate.BusDataAttrUpdate.UpdateBusStatus(
                BusStatus.InMaintenance,
            ),
            "92374924"
        ).also {
            when(it){
                is GetBack.Error -> {println("Failed ........"+it.toString())}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }

        source.getItem((92374924).toString()).also {
            when(it){
                is GetBack.Error -> {println("Failed ........")}
                is GetBack.Success -> { println("Sucess......")
                    println(it.toString())
                }
            }
        }
//        source.transactWriteItems(
//            listOf(
//                DynamoDbTransactWriteItem(
//                    putItem = demData,
//                    deleteItemKey = null
//                ),
//                DynamoDbTransactWriteItem(
//                    putItem = demData2,
//                    deleteItemKey = null
//                )
//            )
//        ).also {
//            when(it){
//                is GetBack.Error -> {println("Failed ........")}
//                is GetBack.Success -> { println("Sucess......")
//                    println(it.toString())
//                }
//            }
//        }
    }

//    val serData = converter.convertFrom(converter.convertTo(demData))
//    println(
//        converter.convertTo(demData)
//    )
//


}