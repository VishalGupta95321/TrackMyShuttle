package app

import models.BusStopsData
import models.BusTrackingData
import models.Coordinates
import models.WindowedCoordinates
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector


class BusTrackingDataProcessingFunction: KeyedCoProcessFunction<String, BusStopsData, WindowedCoordinates, BusTrackingData>() {

    private lateinit var lastSixWindowsAvgSpeed: ListState<String>
    private lateinit var lastSixWindowsCoordinates: ListState<Coordinates>
    private lateinit var farthestStopFromCurrentPoint: ValueState<Coordinates>
    private lateinit var nextStop: ValueState<Coordinates>
    private lateinit var lastNextStop: ValueState<Coordinates>
    private lateinit var currentStop: ValueState<Coordinates>


    override fun open(openContext: OpenContext?) {
        runtimeContext.apply {
            lastSixWindowsAvgSpeed = getListState<String>(lastSixWindowsAvgSpeed::class.simpleName!!)
            lastSixWindowsCoordinates = getListState<Coordinates>(lastSixWindowsCoordinates::class.simpleName!!)
            farthestStopFromCurrentPoint = getValueState<Coordinates>(farthestStopFromCurrentPoint::class.simpleName!!)
            nextStop = getValueState<Coordinates>(nextStop::class.simpleName!!)
            currentStop = getValueState<Coordinates>(currentStop::class.simpleName!!)
            lastNextStop = getValueState<Coordinates>(lastNextStop::class.simpleName!!)
        } }

    override fun processElement1(
        element: BusStopsData?,
        context: KeyedCoProcessFunction<String?, BusStopsData?, WindowedCoordinates?, BusTrackingData?>.Context?,
        out: Collector<BusTrackingData?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun processElement2(
        element: WindowedCoordinates?,
        context: KeyedCoProcessFunction<String?, BusStopsData?, WindowedCoordinates?, BusTrackingData?>.Context?,
        out: Collector<BusTrackingData?>?
    ) {
        TODO("Not yet implemented")
    }

    private inline fun <reified T: Any> RuntimeContext.getValueState(name: String) = getState(ValueStateDescriptor(name, T::class.java))
    private inline fun <reified T: Any>  RuntimeContext.getListState(name: String) = getListState(ListStateDescriptor(name, T::class.java))


}


@Transient
private var oldestCoordinates: ValueState<Coordinates>? = null

@Transient
private var latestCoordinates: ValueState<Coordinates>? = null

/// Just called once upon operator initialization
override fun open(openContext: OpenContext?) {

    /// Instantiating the resources
    val  oldestCoordinatesDescriptor = ValueStateDescriptor<Coordinates>(
        "Oldest Coordinates",
        Coordinates::class.java )
    oldestCoordinates = runtimeContext.getState(oldestCoordinatesDescriptor)

    val  latestCoordinatesDescriptor = ValueStateDescriptor<Coordinates>(
        "Latest Coordinates",
        Coordinates::class.java )
    latestCoordinates = runtimeContext.getState(latestCoordinatesDescriptor)
}

override fun processElement(
    element: BusLocationData?,
    context: KeyedProcessFunction<String?, BusLocationData?, WindowedCoordinates?>.Context?,
    out : Collector<WindowedCoordinates?>?
) {
    if(oldestCoordinates?.value() == null) {

    }
}

