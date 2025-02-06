package com.trackmyshuttle.app.data.network

import com.trackmyshuttle.app.data.model.responce.Bus
import com.trackmyshuttle.app.data.model.responce.BusLocationData
import com.trackmyshuttle.app.data.model.responce.BusStop
import com.trackmyshuttle.app.data.util.GetBack
import com.trackmyshuttle.app.data.util.GetBackBasic
import kotlinx.coroutines.flow.Flow

interface TrackMyShuttleApi{
    suspend fun openConnection()
    suspend fun closeConnection()
    suspend fun isConnectionActive(): GetBackBasic
    suspend fun trackBus(busId:String): Flow<BusLocationData>?
    suspend fun getBusById(busId:String): GetBack<Bus>
    suspend fun getAllBusStops(): GetBack<List<BusStop>>
    suspend fun getBussesByBusStop(
        from: String,
        to: String
    ): GetBack<List<Bus>>
}