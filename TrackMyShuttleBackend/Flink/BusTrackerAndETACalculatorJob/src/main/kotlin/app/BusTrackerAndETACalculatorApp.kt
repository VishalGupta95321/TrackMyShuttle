package app

import kotlinx.serialization.json.Json
import models.BusLocationData
import models.BusData
import models.KafkaSinkConfiguration
import models.KafkaSourceConfiguration
import models.Route
import models.toBusTrackingData
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.getExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import util.BusUnion
import util.serializers.CustomBusLocationSerializer
import util.serializers.CustomBusDataSerializer
import util.serializers.CustomBusRoutesDeserializer
import util.EitherOfThree
import util.serializers.CustomBusETADataSerializer
import util.serializers.CustomBusTrackingDataSerializer
import java.time.Duration


//// Kafka Source Topics
private const val BUS_LOCATION_DATA_TOPIC = "BUS_LOCATION_DATA"
private const val BUS_DATA_TOPIC = "BUS_DATA"
private const val BUS_ROUTES_DATA_TOPIC = "BUS_ROUTES_DATA"


/// Kafka Sink Topics
private const val BUS_ETA_DATA_TOPIC = "BUS_ETA_DATA"
private const val BUS_TRACKING_DATA_TOPIC = "BUS_TRACKING_DATA"


private const val KAFKA_BROKER = "localhost:54302"
private const val BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_SECONDS = 5L
private const val BUS_LOCATION_WITH_METADATA_TUMBLING_WINDOW_SIZE_IN_SECONDS =10L


@Suppress("UNCHECKED_CAST")
class BusTrackerAndETACalculatorApp {

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
            )
            .setDeliveryGuarantee(DeliveryGuarantee.NONE)
            .build()



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

            ///// Bus data
            val busDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_DATA_TOPIC,
                        deserializer = CustomBusDataSerializer(json)
                    )
                },
                WatermarkStrategy.noWatermarks(),
                "BusDataInputStream"
            )

            /// Routes data
            val busRoutesDataInputStream = environment.fromSource(
                getKafkaSource {
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_ROUTES_DATA_TOPIC,
                        deserializer = CustomBusRoutesDeserializer(json)
                    )
                },
                WatermarkStrategy.noWatermarks(),
                "BusRoutesDataInputStream"
            )


            val locationDataStream = busLocationDataInputStream.map { EitherOfThree.Left<BusLocationData>(it) as BusUnion }
            val busDataStream  = busDataInputStream.map { EitherOfThree.Middle<BusData>(it) as BusUnion }
            val routeDataStream = busRoutesDataInputStream.map { EitherOfThree.Right<Route>(it) as BusUnion}

            val busLocationWithMetadataStream = locationDataStream.union(busDataStream,routeDataStream).keyBy {
                when (it) {
                    is EitherOfThree.Left -> it.value.busId
                    is EitherOfThree.Middle -> it.value.busId
                    is EitherOfThree.Right -> it.value.busId
                }
            }.process(BusRouteAndStopDiscoveryProcessFunction())

            val windowedBusLocationDataStream = busLocationWithMetadataStream
                .keyBy { it.busId }
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(BUS_LOCATION_WITH_METADATA_TUMBLING_WINDOW_SIZE_IN_SECONDS)))
                .process(BusLocationDataProcessWindowFunction())



            val busETADataStream = windowedBusLocationDataStream
                .keyBy { it.busId }
                .process(BusETACalculatingProcessFunction())

            val busTrackingDataStream = busLocationWithMetadataStream
                .map { it.toBusTrackingData() }


            val busETADataStreamSink = getKafkaSink {
                KafkaSinkConfiguration(
                    bootstrapServers = KAFKA_BROKER,
                    topic = BUS_ETA_DATA_TOPIC,
                    serializer = CustomBusETADataSerializer(json)
                )
            }

            val busTrackingDataStreamSink = getKafkaSink {
                KafkaSinkConfiguration(
                    bootstrapServers = KAFKA_BROKER,
                    topic = BUS_TRACKING_DATA_TOPIC,
                    serializer = CustomBusTrackingDataSerializer(json)
                )
            }

            busETADataStream.sinkTo(busETADataStreamSink)
            busTrackingDataStream.sinkTo(busTrackingDataStreamSink)
        }
    }
}