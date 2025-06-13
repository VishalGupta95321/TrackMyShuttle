package app

import models.BusData
import models.BusTrackingData
import models.Coordinate
import models.BusLocationWithMetadataWindowed
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector
import util.getValueState
import util.getListState



class BusEtaCalculatingProcessingFunction: KeyedCoProcessFunction<String, BusData, BusLocationWithMetadataWindowed, BusTrackingData>() {

    private lateinit var lastSixWindowsAvgSpeed: ListState<String>
    private lateinit var lastSixWindowsCoordinates: ListState<Coordinate>
    private lateinit var farthestStopFromCurrentPoint: ValueState<Coordinate>
    private lateinit var nextStop: ValueState<Coordinate>
    private lateinit var lastNextStop: ValueState<Coordinate>
    private lateinit var currentStop: ValueState<Coordinate>


    override fun open(openContext: OpenContext?) {
        runtimeContext.apply {
            lastSixWindowsAvgSpeed = getListState<String>(lastSixWindowsAvgSpeed::class.simpleName!!)
            lastSixWindowsCoordinates = getListState<Coordinate>(lastSixWindowsCoordinates::class.simpleName!!)
            farthestStopFromCurrentPoint = getValueState<Coordinate>(farthestStopFromCurrentPoint::class.simpleName!!)
            nextStop = getValueState<Coordinate>(nextStop::class.simpleName!!)
            currentStop = getValueState<Coordinate>(currentStop::class.simpleName!!)
            lastNextStop = getValueState<Coordinate>(lastNextStop::class.simpleName!!)
        } }

    override fun processElement1(
        element: BusData?,
        context: KeyedCoProcessFunction<String?, BusData?, BusLocationWithMetadataWindowed?, BusTrackingData?>.Context?,
        out: Collector<BusTrackingData?>?
    ) {
        TODO("Not yet implemented")
    }

    override fun processElement2(
        element: BusLocationWithMetadataWindowed?,
        context: KeyedCoProcessFunction<String?, BusData?, BusLocationWithMetadataWindowed?, BusTrackingData?>.Context?,
        out: Collector<BusTrackingData?>?
    ) {
        TODO("Not yet implemented")
    }




}


//@Transient
//private var oldestCoordinates: ValueState<Coordinates>? = null
//
//@Transient
//private var latestCoordinates: ValueState<Coordinates>? = null
//
///// Just called once upon operator initialization
//override fun open(openContext: OpenContext?) {
//
//    /// Instantiating the resources
//    val  oldestCoordinatesDescriptor = ValueStateDescriptor<Coordinates>(
//        "Oldest Coordinates",
//        Coordinates::class.java )
//    oldestCoordinates = runtimeContext.getState(oldestCoordinatesDescriptor)
//
//    val  latestCoordinatesDescriptor = ValueStateDescriptor<Coordinates>(
//        "Latest Coordinates",
//        Coordinates::class.java )
//    latestCoordinates = runtimeContext.getState(latestCoordinatesDescriptor)
//}
//
//override fun processElement(
//    element: BusLocationData?,
//    context: KeyedProcessFunction<String?, BusLocationData?, WindowedCoordinates?>.Context?,
//    out : Collector<WindowedCoordinates?>?
//) {
//    if(oldestCoordinates?.value() == null) {
//
//    }
//}
//
