package com.trackmyshuttle.app.data.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createBusTrackingClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                json =  Json {
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                }
            )
        }
        install(WebSockets)
    }
}