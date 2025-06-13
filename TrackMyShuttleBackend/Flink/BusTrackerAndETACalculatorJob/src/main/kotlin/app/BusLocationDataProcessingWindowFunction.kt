package app

import models.BusLocationWithMetadata
import models.BusLocationWithMetadataWindowed
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector


class BusLocationDataProcessingWindowFunction: ProcessWindowFunction<BusLocationWithMetadata, BusLocationWithMetadataWindowed, String, TimeWindow>(){
    override fun process(
        key: String,
        context: ProcessWindowFunction<BusLocationWithMetadata, BusLocationWithMetadataWindowed, String, TimeWindow>.Context,
        elements: Iterable<BusLocationWithMetadata>,
        out: Collector<BusLocationWithMetadataWindowed>
    ) {
        /// Coord filtered by lastPassed Stop and next Stop, making sure we are not mixing up data running on route with another set of stops.

        val firstCoord = elements.first()

        val routeFilteredCoordinates =  elements.filter { it.lastPassedStop == firstCoord.lastPassedStop && it.nextStop == firstCoord.nextStop }
        val timestampSortedCoordinates = routeFilteredCoordinates.sortedBy { it.location.timestamp }

        if(timestampSortedCoordinates.isNotEmpty()){
            val windowedData = BusLocationWithMetadataWindowed(
                busId = key,
                busLocationMetadata = firstCoord,
                oldestCoordinates = timestampSortedCoordinates.first().location,
                latestCoordinates = timestampSortedCoordinates.last().location
            )
            out.collect(windowedData)
        }
    }
}
