import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.DescribeTableRequest
import data.entity.RouteEntity
import data.model.DynamoDbTransactWriteItem
import data.source.DynamoDbDataSource
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

//   runBlocking {
//      describeTable(db)
//
//      source.transactWriteItems(listOf(
//         DynamoDbTransactWriteItem(
//            putItem =
//            u null,
//            null
//         )
//      ))
//
//   }
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
