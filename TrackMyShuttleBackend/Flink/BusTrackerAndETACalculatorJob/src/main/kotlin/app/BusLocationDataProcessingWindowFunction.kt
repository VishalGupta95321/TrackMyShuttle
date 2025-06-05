package app

import models.BusLocationData
import models.WindowedCoordinates
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector


class BusLocationDataProcessingWindowFunction: ProcessWindowFunction<BusLocationData, WindowedCoordinates, String, TimeWindow>(){
    override fun process(
        key: String,
        context: ProcessWindowFunction<BusLocationData, WindowedCoordinates, String, TimeWindow>.Context,
        elements: Iterable<BusLocationData>,
        out: Collector<WindowedCoordinates>
    ) {
        val sortedCoordinates = elements.sortedBy { it.busId }
        if(sortedCoordinates.size > 1){
            val windowedCoordinates = WindowedCoordinates(
                busId = key,
                oldestCoordinates = sortedCoordinates.first().coordinates,
                latestCoordinates = sortedCoordinates.last().coordinates
            )
            out.collect(windowedCoordinates)
        }
    }
}
