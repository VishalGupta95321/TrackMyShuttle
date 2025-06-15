package app

import models.BusLocationData
import models.BusData
import models.BusLocationWithMetadata
import models.KafkaSinkConfiguration
import models.KafkaSourceConfiguration
import models.Route
import models.toBusTrackingData
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.getExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.triggers.ContinuousEventTimeTrigger
import util.BusUnion
import util.serializers.CustomBusLocationSerializer
import util.serializers.CustomBusDataSerializer
import util.serializers.CustomBusRoutesDeserializer
import util.EitherOfThree
import util.serializers.BusETADataKafkaRecordSerializer
import util.serializers.BusTrackingDataKafkaRecordSerializer
import java.time.Duration


//// Kafka Source Topics
private const val BUS_LOCATION_DATA_TOPIC = "BUS_LOCATION_DATA"
private const val BUS_DATA_TOPIC = "BUS_DATA"
private const val BUS_ROUTES_DATA_TOPIC = "BUS_ROUTES_DATA"


/// Kafka Sink Topics
private const val BUS_ETA_DATA_TOPIC = "BUS_ETA_DATA"
private const val BUS_TRACKING_DATA_TOPIC = "BUS_TRACKING_DATA"


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
                        deserializer = CustomBusLocationSerializer()
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
                        deserializer = CustomBusDataSerializer()
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

            val typeInfo = object : TypeHint<EitherOfThree<BusLocationData, BusData, Route>>(){}.typeInfo

            val locationDataStream = busLocationDataInputStream
                .map { EitherOfThree.Left<BusLocationData>(it) as BusUnion }
                .returns(typeInfo)

            val busDataStream  = busDataInputStream
                .map { EitherOfThree.Middle<BusData>(it) as BusUnion }
                .returns(typeInfo)
            val routeDataStream = busRoutesDataInputStream
                .map { EitherOfThree.Right<Route>(it) as BusUnion}
                .returns(typeInfo)

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
                    serializer = BusETADataKafkaRecordSerializer(BUS_ETA_DATA_TOPIC)
                )
            }

            val busTrackingDataStreamSink = getKafkaSink {
                KafkaSinkConfiguration(
                    bootstrapServers = KAFKA_BROKER,
                    serializer = BusTrackingDataKafkaRecordSerializer(BUS_TRACKING_DATA_TOPIC)
                )
            }

            busETADataStream.sinkTo(busETADataStreamSink)
            busTrackingDataStream.sinkTo(busTrackingDataStreamSink)

            environment.execute("BusTrackerAndETACalculatorJob")
        }
    }
}