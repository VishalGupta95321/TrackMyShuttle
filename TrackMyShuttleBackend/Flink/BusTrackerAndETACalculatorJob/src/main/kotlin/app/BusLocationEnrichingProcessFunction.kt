package app

import models.BusData
import models.BusLocationData
import models.RawBusStop
import models.BusStop
import models.EnrichedLocationData
import models.Route
import models.toBusStop
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector
import util.EitherOfThree
import util.getListState

class BusLocationEnrichingProcessFunction: KeyedCoProcessFunction<String,EitherOfThree<RawBusStop, BusData, Route>, BusLocationData, EnrichedLocationData>() {

    private lateinit var busStops: ListState<RawBusStop>
    private lateinit var busRoutes: ListState<Route>
    private lateinit var buses: ListState<BusData>

    override fun open(openContext: OpenContext?) {
        runtimeContext.apply {
            busStops = getListState<RawBusStop>(BUS_STOP_STATE)
            buses = getListState<BusData>(BUSES_STATE)
            busRoutes = getListState<Route>(ROUTES_STATE)
        }
    }
    override fun processElement1(
        element: EitherOfThree<RawBusStop, BusData, Route>,
        context: KeyedCoProcessFunction<String, EitherOfThree<RawBusStop, BusData, Route>, BusLocationData, EnrichedLocationData>.Context,
        out: Collector<EnrichedLocationData>
    ) {
        when (element) {
            is EitherOfThree.BusStop -> busStops.add(element.value)
            is EitherOfThree.Bus -> buses.add(element.value)
            is EitherOfThree.Route -> busRoutes.add(element.value)
        }
    }

    override fun processElement2(
        element: BusLocationData,
        context: KeyedCoProcessFunction<String,EitherOfThree<RawBusStop, BusData, Route>, BusLocationData, EnrichedLocationData>.Context,
        out: Collector<EnrichedLocationData>
    ) {
        val busId = element.busId
        val busesList = buses.get()?.toList() ?: return
        val busRoutesList = busRoutes.get()?.toList() ?: return
        val busStopsList = busStops.get()?.toList() ?: return

        val busData = busesList.toList().find { it.busId == busId } ?: return

        val busStopsData = mutableListOf<BusStop>()

        busData.stopIds.forEach { (stopId,waitTime) ->
            val stop = busStopsList.find { it.stopId == stopId }  ?: return
            busStopsData.add(stop.toBusStop(waitTime))
        }

        val busRoutesData = mutableListOf<Route>()

        busRoutesList.forEach { route ->
            busStopsData.forEach { stop ->
                if(route.fromStopId == stop.stopId || route.toStopId == stop.stopId) {
                    busRoutesData.add(route)
                }
            }
        }

        if (busRoutesData.isNotEmpty() && busStopsData.isNotEmpty())
            out.collect(EnrichedLocationData(element,busData,busStopsData,busRoutesData))
    }

    companion object{
        private const val BUSES_STATE = "BUSES_STATE"
        private const val ROUTES_STATE = "ROUTES_STATE"
        private const val BUS_STOP_STATE = "BUS_STOP_STATE"
    }
}



