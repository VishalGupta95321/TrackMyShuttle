package app

import models.BusETAData
import models.BusLocationWithMetadataWindowed
import org.apache.flink.api.common.functions.OpenContext
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import kotlin.time.Duration.Companion.milliseconds

class BusETACalculatingProcessFunction: KeyedProcessFunction<String, BusLocationWithMetadataWindowed, BusETAData>() {

    private lateinit var etaCalculator : EtaCalculator

    override fun open(openContext: OpenContext?) {
        etaCalculator = EtaCalculator()
    }

    override fun processElement(
        element: BusLocationWithMetadataWindowed,
        context: KeyedProcessFunction<String, BusLocationWithMetadataWindowed, BusETAData>.Context,
        out: Collector<BusETAData>
    ) {

        val currRoute = element.busLocationMetadata.currentRoute

        val currentRouteDetails = CurrentRouteDetails(
            busId = element.busId,
            routeId = currRoute.routeId,
            coordinates = currRoute.coordinates,
            duration = currRoute.duration,
            distanceInMeters = currRoute.distanceInMeters,
            routeType = element.busLocationMetadata.routeType,
            isReturning = element.busLocationMetadata.isReturning,
        )

        val lastPassedStop = element.busLocationMetadata.lastPassedStop.second
        val lastPassedArrivalTime = element.busLocationMetadata.lastPassedStop.first?.milliseconds

        val fromStopDetails = FromStopDetails(
            latitude = lastPassedStop.coordinates.latitude,
            longitude = lastPassedStop.coordinates.longitude,
            waitTime = lastPassedStop.waitTime,
            arrivalTime = lastPassedArrivalTime
        )
        val etaData = etaCalculator.calculateEta(
            oldestCoordinates = element.oldestCoordinates,
            latestCoordinate = element.latestCoordinates,
            currentRoute = currentRouteDetails,
            fromStop = fromStopDetails,
        )

        etaData?.let {
            out.collect(
                BusETAData(
                    busId = element.busId,
                    eta = etaData,
                )
            )
        }
    }
}