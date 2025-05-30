package di

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import controller.BusController
import controller.BusControllerImpl
import data.db_converters.BusItemConverter
import data.entity.BusEntity
import data.respository.BusRepository
import data.respository.BusRepositoryImpl
import data.source.DynamoDbDataSourceImpl
import data.source.DynamoDbDataSource
import data.util.ClassIntrospector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module


val MainModule = module  {

    single<DynamoDbClient> {
        val client = DynamoDbClient { region = "ap-south-1" }
        client
    }

    single<DynamoDbDataSource<BusEntity>> {
        DynamoDbDataSourceImpl(
            clazz = BusEntity::class,
            databaseClient = get(),
            introspector = ClassIntrospector(BusEntity::class),
            itemConverter = BusItemConverter(),
        )
    }

    single<BusRepository> {
        BusRepositoryImpl(get())
    }

    single<BusController>{
        BusControllerImpl(get())
    }

    single<CoroutineScope>() {
        named("IOSupervisorCoroutineScope")
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    single<CoroutineScope> {
        named("DefaultSupervisorCoroutineScope")
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    single<Json>{
        Json{ignoreUnknownKeys = true}
    }

}
