import aws.sdk.kotlin.services.dynamodb.DynamoDbClient

import aws.sdk.kotlin.services.dynamodb.model.DescribeTableRequest
import aws.sdk.kotlin.services.dynamodb.model.ScanRequest
import data.entity.RouteEntity
import data.respository.RouteRepository
import data.source.DynamoDbDataSource
import data.util.GetBack
import di.MainModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject


fun main(){


   startKoin {
      modules(MainModule)
   }

   val db : DynamoDbClient by inject(clazz = DynamoDbClient::class.java)
   createRoutes(listOf("Stop1", "Stop2", "Stop3", "Stop4"))

   val source : DynamoDbDataSource<RouteEntity> by inject(
      clazz = DynamoDbDataSource::class.java,
   )
   val repo : RouteRepository by inject(
      clazz = RouteRepository::class.java,
   )

///////////////////////////////////////////////
   runBlocking {
      val BUS_ID = "BUS-0055"

//      repo.assignRoutesToBusAndUpdateBusTable(
//         busId =BUS_ID,
//         oldStopIds = listOf(),
//         newStopsIds = listOf("STOP2", "STOP3", "STOP4", "STOP5","STOP6"),
//      ).let { result->
//         when (result) {
//            is GetBack.Success -> {println("Success")}
//            is GetBack.Error-> {println("Error ${result.message}")}
//         }
//      }

      repo.deleteAssignedRoutesForBusAndUpdateBusTable(
         busId = "BUS-0055",
         allStopIds = listOf( "STOP2", "STOP3", "STOP4","STOP6"),
         stopIdsToDelete = listOf("STOP4")
      )

      repo.getBusIdsByRouteId("STOP4-STOP5").let {
         when(it){
            is GetBack.Error -> {println("Error ========== ${it.message}")}
            is GetBack.Success -> {
               println("BUSIDS ============ ${it.data}")
            }
         }
      }

      val scanRequest = ScanRequest {
         tableName = "ROUTE_TABLE"
      }
      val response = db.scan(scanRequest)
      val items = response.items
      println("Route Items =  ${items?.size}")
      items?.forEach { item ->
         println(item)
      }


      val scanRequest2 = ScanRequest {
         tableName = "BUS_TABLE"
      }
      val response2 = db.scan(scanRequest2)
      val items2 = response2.items
      println("Items =  ${items2?.size}")
      items2?.forEach { item ->
         if (item["busId"]?.asS() == "BUS-066") {
            println("BusId ===== "+ item["busId"])
            println("Stops ======= "+item["stopIds"])
         }
      }

      describeTable(db)

   }
///////////////////////////////






   //////////////////

//   val old = listOf<String>() /// cant be less than 2
//   val new = listOf<String>("Stop6","Stop7")   /// at least 2 with or without old stops
//   val set =  mutableSetOf<String>()
//   //set.addAll(generatePossibleRoutes(old))
//   val allRoutes = set.addAll(generatePossibleRoutes(old+new))
//   val routesInclNewStopIds = set.removeAll(generatePossibleRoutes(old))
//
//
//   set.forEachIndexed { i,j->
//      println("Route No ${i+1} = $j")
//   }

   ////////////////




////////////////////////////////
//   runBlocking {
//      val deleteTableRequest = DeleteTableRequest {
//         tableName = "ROUTE_TABLE"
//      }
//      db.deleteTable(deleteTableRequest)
//   }
///////////////////////



   delRoutes(setOf("Stop3", "Stop4", "Stop5","Stop6","Stop7"),setOf("Stop5"))
}



private fun delRoutes(
   allStopIds: Set<String>,
   stopIdsToDelete: Set<String>,
) {


   val allRoutes = generatePossibleRoutes(allStopIds.toList())
   val routesExclDelStops = generatePossibleRoutes(allStopIds.minus(stopIdsToDelete).toList())

   println("All ROUTES ===== $allRoutes")
   println("All ROUTES EXCL DEL STOP===== $routesExclDelStops")

   val routesToDelete = allRoutes.minus(routesExclDelStops)
   println("Routes to delete ===== $routesToDelete")
}

//private fun delRoutes(){
//   val allStopIds = mutableSetOf<String>()
//   allStopIds.addAll(listOf("Stop2", "Stop3", "Stop4", "Stop5"))
//
//   val stopIdsToDelete = mutableSetOf<String>()
//   stopIdsToDelete.addAll(listOf("Stop5"))
//
//   val allRoutes = generatePossibleRoutes(allStopIds.toList())
//   val a = allStopIds.minus(stopIdsToDelete).toList()
//   println("A ===== $a")
//   val routesExclDelStops = generatePossibleRoutes(a)
//
//   println("All ROUTES ===== $allRoutes")
//
//   println("All ROUTES EXCL DEL STOP===== $routesExclDelStops")
//
//   val routesToDelete = allRoutes.minus(routesExclDelStops).toList()
//   println("Routes to delete ===== $routesToDelete")
//}



private fun generatePossibleRoutes(
   stopIds: List<String>,
): List<String> {
   println(stopIds)
   val routes  = mutableListOf<String>()
   stopIds.forEachIndexed { i, _ ->
      stopIds.forEachIndexed { j, _ ->
                if (i < j) {
                   routes.add("${stopIds[i]+ "-" + stopIds[j]} ")
                   // println("${stopIds[i]+ "-" +stopIds[j]} ")
                }
            }
        }
   return routes
}

private fun generateNewPossibleRoutes(
   oldStopIds: List<String>,
   newStopId: String
){
   val newRoutes = mutableListOf<String>()

   oldStopIds.forEachIndexed { i,_ ->
     newRoutes.add(oldStopIds[i]+newStopId)
   }
}


fun createRoutes(stopIds: List<String>){

}

suspend fun describeTable(db: DynamoDbClient){
   val describeTableRequest = DescribeTableRequest {
      tableName = "ROUTE_TABLE"
   }

   val response = db.describeTable(describeTableRequest)
   println(response.table)
}


