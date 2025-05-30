package di

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import controller.BusStopController
import controller.BusStopControllerImpl
import data.db_converters.BusStopItemConverter
import data.db_converters.BusStopScanResponseItemConverter
import data.entity.BusStopEntity
import data.model.BusStopScanned
import data.respository.BusStopRepository
import data.respository.BusStopRepositoryImpl
import data.source.DynamoDbDataSource
import data.source.DynamoDbDataSourceImpl
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

    single<DynamoDbDataSource<BusStopEntity, BusStopScanned>> {
        DynamoDbDataSourceImpl(
            clazz = BusStopEntity::class,
            databaseClient = get(),
            introspector = ClassIntrospector(BusStopEntity::class),
            itemConverter = BusStopItemConverter(),
            itemConverter2 = BusStopScanResponseItemConverter()
        )
    }

    single<BusStopRepository> {
        BusStopRepositoryImpl(get())
    }

    single<BusStopController>{
        BusStopControllerImpl(get())
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
        Json
    }

}
