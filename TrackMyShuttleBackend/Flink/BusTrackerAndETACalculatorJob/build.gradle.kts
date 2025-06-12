import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val flinkVersion: String by project



plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "2.0.21"

}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven{
        url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
    }
}




java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


dependencies {
    testImplementation(kotlin("test"))

    implementation("org.apache.flink:flink-java:${flinkVersion}")
    implementation("org.apache.flink:flink-streaming-java:${flinkVersion}")
    implementation("org.apache.flink:flink-connector-kafka:3.4.0-1.20")
    implementation("org.apache.flink:flink-connector-base:${flinkVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    /// MapBox Turf Module
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:7.4.0")

    /// Remove it if not needed.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

}

tasks.test {
    useJUnitPlatform()
}


tasks.jar {
    manifest {
        attributes(
            "Main-Class" to ""
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}


tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}