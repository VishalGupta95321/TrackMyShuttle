package di

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import com.google.gson.Gson
import controller.RouteController
import controller.RouteControllerImpl
import data.db_converters.RouteItemConverter
import data.entity.RouteEntity
import data.respository.RouteRepository
import data.respository.RouteRepositoryImpl
import data.source.DynamoDbDataSource
import data.source.DynamoDbDataSourceImpl
import data.util.ClassIntrospector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module


val MainModule = module  {

    single<DynamoDbClient> {
        val client = DynamoDbClient { region = "ap-south-1" }
        client
    }

    single<DynamoDbDataSource<RouteEntity>> {
        DynamoDbDataSourceImpl(
            clazz = RouteEntity::class,
            databaseClient = get(),
            introspector = ClassIntrospector(RouteEntity::class),
            itemConverter = RouteItemConverter(),
        )
    }

    single<RouteRepository> {
        RouteRepositoryImpl(get())
    }

    single<RouteController>{
        RouteControllerImpl(get())
    }

    single<CoroutineScope>() {
        named("IOSupervisorCoroutineScope")
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    single<CoroutineScope> {
        named("DefaultSupervisorCoroutineScope")
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    single<Gson>{
        Gson()
    }
}
