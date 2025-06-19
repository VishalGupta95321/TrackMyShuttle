package org.example.di

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import data.db_converters.RouteItemConverter
import data.entity.RouteEntity
import org.example.data.source.dynamo_db.DynamoDbDataSource
import org.example.data.source.dynamo_db.DynamoDbDataSourceImpl
import data.util.ClassIntrospector
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val MainModule = module {

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

//    single<RouteRepository> {
//        BusRepositoryImpl(Scope.get())
//    }

//    Module.single<BusController> {
//        BusControllerImpl(Scope.get())
//    }

    single<CoroutineScope>() {
        named("IOSupervisorCoroutineScope")
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    single<CoroutineScope> {
        named("DefaultSupervisorCoroutineScope")
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    single<Json> {
        Json { ignoreUnknownKeys = true }
    }

    single<HttpClient> {
        HttpClient(){
            install(ContentNegotiation) {
                json()
            }
        }
    }
}
