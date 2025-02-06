package com.trackmyshuttle.app.data.network

import com.trackmyshuttle.app.data.model.responce.BasisResponse
import com.trackmyshuttle.app.data.model.responce.Bus
import com.trackmyshuttle.app.data.model.responce.BusLocationData
import com.trackmyshuttle.app.data.model.responce.BusStop
import com.trackmyshuttle.app.data.util.GetBack
import com.trackmyshuttle.app.data.util.GetBackBasic
import com.trackmyshuttle.app.data.util.createBusTrackingClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.isSuccess
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json

private const val  BASE_URL = "ws://192.168.29.108:8085/"

class TrackMyShuttleImpl: TrackMyShuttleApi {

    private var session: WebSocketSession? = null
    private val client: HttpClient = createBusTrackingClient()
    private val json: Json = Json

    override suspend fun openConnection() {
       session = client.webSocketSession {
          url("$BASE_URL/track")
       }
    }

    override suspend fun closeConnection() {
        session?.close()
    }

    override suspend fun isConnectionActive(): GetBackBasic {
      return if (session==null) GetBack.Error() else GetBack.Success()
    }

    override suspend fun trackBus(
        busId:String
    ): Flow<BusLocationData>? {
         session?.send(Frame.Text(busId))
         session?.let {  webSocketSession ->
             webSocketSession.incoming
                 .consumeAsFlow()
                 .filterIsInstance<Frame.Text>()
                 .mapNotNull { json.decodeFromString<BusLocationData>(it.readText()) }
         } .also { return it }
    }

    override suspend fun getBusById(busId: String): GetBack<Bus> {
        val result = client.get("$BASE_URL/bus") {
            url{
                parameters.append("busId",busId)
            }
        }
        return if (result.status.isSuccess())
            GetBack.Success(result.body<Bus>())
        else
            GetBack.Error()
    }

    override suspend fun getAllBusStops(): GetBack<List<BusStop>> {
        val result = client.get("$BASE_URL/stops")

        return if (result.status.isSuccess())
           GetBack.Success(result.body<BasisResponse<BusStop>>().data)
        else
            GetBack.Error()
    }

    override suspend fun getBussesByBusStop(from: String, to: String): GetBack<List<Bus>> {
        val result = client.get("$BASE_URL/buses") {
            url{
                parameters.append("from",from)
                parameters.append("to",to)
            }
        }

        return if (result.status.isSuccess())
            GetBack.Success(result.body<BasisResponse<Bus>>().data)
        else
            GetBack.Error()
    }
}