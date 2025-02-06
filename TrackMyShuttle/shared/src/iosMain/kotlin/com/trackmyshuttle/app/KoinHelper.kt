package com.trackmyshuttle.app

import com.trackmyshuttle.app.data.network.TrackMyShuttleApi
import com.trackmyshuttle.app.data.network.TrackMyShuttleImpl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class KoinHelper: KoinComponent {

    private val busTrackingApi: TrackMyShuttleApi by inject<TrackMyShuttleApi>()

    fun getBusTrackingApi() = busTrackingApi

    fun initKoin(){
        startKoin {
            modules(module {
                single<TrackMyShuttleApi> {
                    TrackMyShuttleImpl()
                }
            })
        }
    }
}