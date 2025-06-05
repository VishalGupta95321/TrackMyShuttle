package app

import kotlinx.serialization.json.Json
import models.BusLocationData
import models.KafkaSinkConfiguration
import models.KafkaSourceConfiguration
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.getExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import util.CustomBusLocationSerializer
import java.time.Duration

private const val BUS_LOCATION_DATA_TOPIC = "BUS_LOCATION_DATA"
private const val BUS_STOPS_DATA_TOPIC = "BUS_STOPS_DATE"
private const val KAFKA_BROKER = "localhost:9092"
private const val BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_SECONDS = 5L
private const val BUS_LOCATION_TUMBLING_WINDOW_SIZE_IN_SECONDS =10L

class BusTrackerAndETACalculator {
    companion object {

        private val json = Json {ignoreUnknownKeys = true}

        private fun <T> getKafkaSource(
            configure: () -> KafkaSourceConfiguration<T>,
        ) = KafkaSource.builder<T>()
            .setBootstrapServers(configure().bootstrapServers)
            .setTopics(configure().topic)
            .setValueOnlyDeserializer(configure().deserializer)
            .setStartingOffsets(OffsetsInitializer.latest())
            .build()

        private fun <T> getKafkaSink(
            configure: () -> KafkaSinkConfiguration<T>,
        ) = KafkaSink.builder<T>()
            .setBootstrapServers(configure().bootstrapServers)
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<T>()
                    .setTopic(configure().topic)
                    .setValueSerializationSchema(configure().serializer)
                    .build()
            ).build()

        @JvmStatic
        fun main(args: Array<String>) {

            val environment = getExecutionEnvironment()

            val busLocationDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_LOCATION_DATA_TOPIC,
                        deserializer = CustomBusLocationSerializer(json)
                    )},
                WatermarkStrategy
                    .forBoundedOutOfOrderness<BusLocationData>(Duration.ofSeconds(BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_SECONDS)
                ).withTimestampAssigner {element,_ -> element.timeStamp.toLong() },
                "BusLocationDataInputStream")

            val windowedBusLocationDataStream = busLocationDataInputStream
                .keyBy { it.busId }
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(BUS_LOCATION_TUMBLING_WINDOW_SIZE_IN_SECONDS)))
                .process(BusLocationDataProcessingWindowFunction())
                .keyBy { it.busId }



            val busStopsDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_STOPS_DATA_TOPIC,
                        deserializer = CustomBusLocationSerializer(json)
                    )
                },
                WatermarkStrategy.noWatermarks(),
                "BusStopsDataInputStream"
            ).keyBy { it.busId }

//            val processedBusTrackingData = busStopsDataInputStream
//                .connect(windowedBusLocationDataStream)
//                .keyBy({it.busId}, { it.busId})
//                .process()



        }
    }
}