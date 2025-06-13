package app

import kotlinx.serialization.json.Json
import models.BusLocationData
import models.BusData
import models.KafkaSinkConfiguration
import models.KafkaSourceConfiguration
import models.Route
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.getExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import util.CustomBusLocationSerializer
import util.CustomBusRoutesSerialization
import util.CustomBusStopsSerializer
import util.EitherOfThree
import java.time.Duration

private const val BUS_LOCATION_DATA_TOPIC = "BUS_LOCATION_DATA"
private const val BUS_STOPS_DATA_TOPIC = "BUS_STOPS_DATA"
private const val BUS_ROUTES_DATA_TOPIC = "BUS_ROUTES_DATA"

private const val KAFKA_BROKER = "localhost:9092"
private const val BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_SECONDS = 5L
private const val BUS_LOCATION_TUMBLING_WINDOW_SIZE_IN_SECONDS =10L

typealias BusUnion = EitherOfThree<BusLocationData, BusData, Route>

@Suppress("UNCHECKED_CAST")
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

            //// Location data
            val busLocationDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_LOCATION_DATA_TOPIC,
                        deserializer = CustomBusLocationSerializer(json)
                    )},
                WatermarkStrategy
                    .forBoundedOutOfOrderness<BusLocationData>(Duration.ofSeconds(BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_SECONDS)
                ).withTimestampAssigner {element,_ -> element.timestamp.toLong() },
                "BusLocationDataInputStream")
                .keyBy { it.busId }

            ///// Stops data
            val busStopsDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_STOPS_DATA_TOPIC,
                        deserializer = CustomBusStopsSerializer(json)
                    )
                },
                WatermarkStrategy.noWatermarks(),
                "BusStopsDataInputStream"
            ).keyBy { it.busId }

            /// Routes data
            val busRoutesDataInputStream = environment.fromSource(
                getKafkaSource {
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_ROUTES_DATA_TOPIC,
                        deserializer = CustomBusRoutesSerialization(json)
                    )
                },
                WatermarkStrategy.noWatermarks(),
                "BusRoutesDataInputStream"
            ).keyBy { it.busId }


            /// All three are keyed

            val streamLocation = busLocationDataInputStream.map { EitherOfThree.Left<BusLocationData>(it) as BusUnion }
            val streamBusStops  = busStopsDataInputStream.map { EitherOfThree.Middle<BusData>(it) as BusUnion }
            val streamBusRoutes = busRoutesDataInputStream.map { EitherOfThree.Right<Route>(it) as BusUnion}

            val combinedInputStream = streamLocation.union(streamBusStops,streamBusRoutes).keyBy {
                when (it) {
                    is EitherOfThree.Left -> it.value.busId
                    is EitherOfThree.Middle -> it.value.busId
                    is EitherOfThree.Right -> it.value.busId
                }
            }.process(BusRouteAndStopDiscoveryProcessFunction())


















            val windowedBusLocationDataStream = busLocationDataInputStream
                //  .keyBy { it.busId } // I dont think i need to keyby() here because the above is already a keyed stream.
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(BUS_LOCATION_TUMBLING_WINDOW_SIZE_IN_SECONDS)))
                .process(BusLocationDataProcessingWindowFunction())
                .keyBy { it.busId }



            val processedBusTrackingData = busStopsDataInputStream
                .connect(windowedBusLocationDataStream)
                .keyBy({it.busId}, { it.busId})
               // .process()

        }
    }
}