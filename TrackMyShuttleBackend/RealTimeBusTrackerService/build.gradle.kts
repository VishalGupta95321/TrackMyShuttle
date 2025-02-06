import aws.sdk.kotlin.hll.codegen.rendering.Visibility
import aws.sdk.kotlin.hll.dynamodbmapper.codegen.annotations.DestinationPackage
import aws.smithy.kotlin.runtime.ExperimentalApi

val koinVersion: String by project
val awsSdkVersion: String by project


plugins {
    kotlin("jvm") version "2.0.21"
    id("aws.sdk.kotlin.hll.dynamodbmapper.schema.generator") version "1.3.76-beta"

}

group = "com.trackmyshuttle.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

@OptIn(ExperimentalApi::class)
dynamoDbMapper {
    destinationPackage = DestinationPackage.Relative("com.trackmyshuttle.app")
    visibility = Visibility.INTERNAL
}


dependencies {
    testImplementation(kotlin("test"))

    // Serialization
    implementation("com.google.code.gson:gson:2.11.0")

    // DynamoDb
    implementation("aws.sdk.kotlin:dynamodb:$awsSdkVersion")

    // DynamoDb Mapper
    implementation("aws.sdk.kotlin:dynamodb-mapper:${awsSdkVersion}-beta")
    implementation("aws.sdk.kotlin:dynamodb-mapper-annotations:${awsSdkVersion}-beta")

    // Lambda
    implementation("com.amazonaws:aws-java-sdk-lambda:1.12.780")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-events:3.14.0")

    // Koin
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // kotlin reflect
    implementation(kotlin("reflect"))


}
// To set the main class
//tasks.jar {
//    manifest {
//        attributes(
//            "Main-Class" to "request_handler.SimpleHandler"
//        )
//    }
//}

// packaging with all the dependencies and runtime
tasks.register<Zip>("packageJar") {
    into("lib") {
        from(tasks.jar)
        from(configurations.runtimeClasspath)
    }
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}