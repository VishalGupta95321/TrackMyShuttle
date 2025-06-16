package app

import models.BusLocationData
import org.apache.flink.api.common.eventtime.Watermark
import org.apache.flink.api.common.eventtime.WatermarkGenerator
import org.apache.flink.api.common.eventtime.WatermarkOutput
import java.time.Duration
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


/// Let's assume bus will send data every 2 secs and it suddenly went offline , in that case the new watermark won't advance
// and the data collected in window won't get collected and proceed either or window won't close. So to tackle that im implementing custom watermark
// generator which will emit watermarks if.   ***** Actually bus have not assigned a particular partition So in that case if
// any of the partition goes Idle and not a particular bus.

class CustomFallBackBoundedOutOfOrdernessWatermark(
    private val maxOutOfOrdernessInMillis: Long,
    private val maxIdlenessTimeoutMillis: Long
): WatermarkGenerator<BusLocationData> {

    private var currentMaxTimestamp = 0L
    private var lastEmit = System.currentTimeMillis()

    override fun onEvent(
        event: BusLocationData,
        eventTimeStamp: Long,
        output: WatermarkOutput
    ) {
        currentMaxTimestamp = max(currentMaxTimestamp,eventTimeStamp)
        lastEmit = System.currentTimeMillis()
        println(" DATA =================== $event")
    }

    override fun onPeriodicEmit(output: WatermarkOutput) {
        val now = System.currentTimeMillis()
        println(" NOW - LAST EMIT = ======== ${(now - lastEmit).milliseconds} ")

        if ((now - lastEmit) > maxIdlenessTimeoutMillis ) {
            println(" TIME OUT =======================================")
            output.emitWatermark(Watermark( now - maxOutOfOrdernessInMillis - 1))
            return@onPeriodicEmit
        }
        println(" NO =========  TIME OUT =======================================")
        output.emitWatermark(Watermark(currentMaxTimestamp - maxOutOfOrdernessInMillis - 1))
    }
}