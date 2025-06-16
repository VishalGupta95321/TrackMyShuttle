package app

import models.BusLocationData
import models.BusData
import models.BusStop
import models.KafkaSinkConfiguration
import models.KafkaSourceConfiguration
import models.Route
import models.toBusTrackingData
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.getExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import util.CombinedStream
import util.serializers.CustomBusLocationDeserializer
import util.serializers.CustomBusDataDeserializer
import util.serializers.CustomBusRoutesDeserializer
import util.EitherOfThree
import util.serializers.BusETADataKafkaRecordSerializer
import util.serializers.BusTrackingDataKafkaRecordSerializer
import util.serializers.BusUpdatesRecordSerializer
import util.serializers.CustomBusStopDataDeserializer
import java.time.Duration


//// Kafka Source Topics
private const val BUS_LOCATION_DATA_TOPIC = "BUS_LOCATION_DATA"
private const val BUS_DATA_TOPIC = "BUS_DATA"
private const val BUS_STOP_DATA_TOPIC = "BUS_STOP_DATA"
private const val BUS_ROUTES_DATA_TOPIC = "BUS_ROUTES_DATA"


/// Kafka Sink Topics
private const val BUS_ETA_DATA_TOPIC = "BUS_ETA_DATA"
private const val BUS_TRACKING_DATA_TOPIC = "BUS_TRACKING_DATA"
private const val BUS_UPDATES_TOPIC = "BUS_UPDATES"


private const val KAFKA_BROKER = "192.168.29.70:9092"
private const val BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_MILLIS = 5000L
private const val BUS_LOCATION_STREAM_MAX_IDLENESS_TIMEOUT_IN_MILLIS = 5000L  /// In case any partition is Idle //// Greater
// then BUS interval of sending location also take partitions in account. For ex- 3 partition = 2sec(bus location interval) * 3 part = 6 sec , idleness should be >=
private const val BUS_LOCATION_WITH_METADATA_TUMBLING_WINDOW_SIZE_IN_SECONDS =10L


@Suppress("UNCHECKED_CAST")
class BusTrackerAndETACalculatorApp {

    companion object {

        private fun <T> getKafkaSource(
            configure: () -> KafkaSourceConfiguration<T>,
        ) = KafkaSource.builder<T>()
            .setBootstrapServers(KAFKA_BROKER)
            .setGroupId("FlinkGroup")
            .setTopics(configure().topic)
            .setValueOnlyDeserializer(configure().deserializer)
            .setStartingOffsets(OffsetsInitializer.latest())
            .build()

        private fun <T> getKafkaSink(
            configure: () -> KafkaSinkConfiguration<T>,
        ) = KafkaSink.builder<T>()
            .setBootstrapServers(KAFKA_BROKER)
            .setRecordSerializer(configure().serializer)
            .setDeliveryGuarantee(DeliveryGuarantee.NONE)
            .build()



        @JvmStatic
        fun main(args: Array<String>) {

            val environment = getExecutionEnvironment().setParallelism(3)

            //// Location data /// BUS will send data every 2 sec lets assume
            val busLocationDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_LOCATION_DATA_TOPIC,
                        deserializer = CustomBusLocationDeserializer()
                    )},
                WatermarkStrategy.forGenerator { c ->
                    CustomFallBackBoundedOutOfOrdernessWatermark(
                        BUS_LOCATION_STREAM_MAX_OUT_OF_ORDERNESS_IN_MILLIS,
                        BUS_LOCATION_STREAM_MAX_IDLENESS_TIMEOUT_IN_MILLIS,
                    )
                }.withTimestampAssigner {element,_ -> element.timestamp.toLong() }.withIdleness(Duration.ofMillis(BUS_LOCATION_STREAM_MAX_IDLENESS_TIMEOUT_IN_MILLIS)),
                "BusLocationDataInputStream")

            ///// Bus data
            val busDataInputStream = environment.fromSource(
                getKafkaSource{
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_DATA_TOPIC,
                        deserializer = CustomBusDataDeserializer()
                    )
                },
                WatermarkStrategy.noWatermarks<BusData>().withIdleness(Duration.ofSeconds(1L)),
                "BusDataInputStream"
            )

            /// Routes data
            val busRoutesDataInputStream = environment.fromSource(
                getKafkaSource {
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_ROUTES_DATA_TOPIC,
                        deserializer = CustomBusRoutesDeserializer()
                    )
                },
                WatermarkStrategy.noWatermarks<Route>().withIdleness(Duration.ofSeconds(1L)),
                "BusRoutesDataInputStream"
            )

            /// Stops Data
            val busStopsDataInputStream = environment.fromSource(
                getKafkaSource {
                    KafkaSourceConfiguration(
                        bootstrapServers = KAFKA_BROKER,
                        topic = BUS_STOP_DATA_TOPIC,
                        deserializer = CustomBusStopDataDeserializer()
                    )
                },
                WatermarkStrategy.noWatermarks<BusStop>().withIdleness(Duration.ofSeconds(1L)),
                "BusStopsDataInputStream"

            ) /// TODO Here



            val typeInfo = object : TypeHint<EitherOfThree<BusStop, BusData, Route>>(){}.typeInfo

//            val locationDataStream = busLocationDataInputStream               ///// DELETE
//                .map { EitherOfThree.Left<BusLocationData>(it) as BusUnion }
//                .returns(typeInfo)

            val locationDataStream = busLocationDataInputStream

            /// Left
            val busStopDataStream = busStopsDataInputStream
                .map { EitherOfThree.BusStop<BusStop>(it) as CombinedStream }
                .returns(typeInfo)

            /// Middle
            val busDataStream  = busDataInputStream
                .map { EitherOfThree.Bus<BusData>(it) as CombinedStream }
                .returns(typeInfo)

            // Right
            val routeDataStream = busRoutesDataInputStream
                .map { EitherOfThree.Route<Route>(it) as CombinedStream}
                .returns(typeInfo)



            val combinedDataStream = busStopDataStream.union(busDataStream,routeDataStream)


            val enrichedBusLocationStream = combinedDataStream
                .connect<BusLocationData>(locationDataStream)
                .keyBy({"1"},{"1"})   //// changed //// It works but not really scalable I had to use key by because I cant use keyed state, and operator state stays in heap.
                /// For now its working same as Broadcast state kind of, I didn't use broadcast state because it also stays in heap. /// Will find out the solution later but for now its working. FIXME
                .process(BusLocationEnrichingProcessFunction())
                .setParallelism(1)


            val busLocationDataWithMetadataStream = enrichedBusLocationStream
                .keyBy { it.busData.busId }
                .process(BusRouteAndStopDiscoveryProcessFunction())


            val windowedBusLocationDataStream = busLocationDataWithMetadataStream
                .keyBy { it.busId }
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(BUS_LOCATION_WITH_METADATA_TUMBLING_WINDOW_SIZE_IN_SECONDS)))
                .process(BusLocationDataProcessWindowFunction())


            val busETADataStream = windowedBusLocationDataStream
                .keyBy { it.busId }
                .process(BusETACalculatingProcessFunction())

            val busTrackingDataStream = busLocationDataWithMetadataStream
                .map { it.toBusTrackingData() }

            val busUpdatesDataStream = busTrackingDataStream.keyBy {
                it.busId
            }.process(BusUpdatesProcessFunction())


            val busETADataStreamSink = getKafkaSink {
                KafkaSinkConfiguration(
                    bootstrapServers = KAFKA_BROKER,
                    serializer = BusETADataKafkaRecordSerializer(BUS_ETA_DATA_TOPIC)
                )
            }

            val busTrackingDataStreamSink = getKafkaSink {
                KafkaSinkConfiguration(
                    bootstrapServers = KAFKA_BROKER,
                    serializer = BusTrackingDataKafkaRecordSerializer(BUS_TRACKING_DATA_TOPIC)
                )
            }

            val busUpdatesStreamSink = getKafkaSink {
                KafkaSinkConfiguration(
                    bootstrapServers = KAFKA_BROKER,
                    serializer = BusUpdatesRecordSerializer(BUS_UPDATES_TOPIC)
                )
            }


            busETADataStream.sinkTo(busETADataStreamSink)
            busTrackingDataStream.sinkTo(busTrackingDataStreamSink)
            busUpdatesDataStream.sinkTo(busUpdatesStreamSink)

            environment.execute("BusTrackerAndETACalculatorJob")
        }
    }
}