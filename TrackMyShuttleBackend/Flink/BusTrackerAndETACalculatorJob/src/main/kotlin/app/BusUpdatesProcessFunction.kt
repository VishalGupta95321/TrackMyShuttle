package app

import models.BusTrackingData
import models.BusUpdates
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import util.getValueState

class BusUpdatesProcessFunction: KeyedProcessFunction<String,BusTrackingData, BusUpdates>(){

    private lateinit var busUpdates: ValueState<BusUpdates>


    override fun open(openContext: OpenContext?) {
        runtimeContext.apply {
            busUpdates = getValueState<BusUpdates>(BUS_UPDATES)
        }
    }
    override fun processElement(
        element: BusTrackingData,
        context: KeyedProcessFunction<String, BusTrackingData, BusUpdates>.Context,
        out: Collector<BusUpdates>
    ) {
        val updates = busUpdates.value()

        if (updates == null || (updates.nextStopId != element.nextStopId && updates.lastStopId != element.lastPassedStopId)) {
            busUpdates.update(
                BusUpdates(
                    busId = element.busId,
                    nextStopId = element.nextStopId,
                    lastStopId = element.lastPassedStopId
                )
            )
            out.collect(busUpdates.value())
        }
    }

    companion object{
        private const val BUS_UPDATES = "BUS_UPDATES"
    }
}