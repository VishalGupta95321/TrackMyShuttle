package util.serializers

import kotlinx.serialization.json.Json
import models.BusTrackingData
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.kafka.clients.producer.ProducerRecord

class BusTrackingDataKafkaRecordSerializer(
    private val topic: String,
): KafkaRecordSerializationSchema<BusTrackingData> {

    @Transient private lateinit var json: Json

    override fun open(
        context: SerializationSchema.InitializationContext?,
        sinkContext: KafkaRecordSerializationSchema.KafkaSinkContext?
    ) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun serialize(
        element: BusTrackingData,
        context: KafkaRecordSerializationSchema.KafkaSinkContext,
        timestamp: Long
    ): ProducerRecord<ByteArray, ByteArray> {
        val key = element.busId.toByteArray()
        val value = json.encodeToString(element).toByteArray()

        return ProducerRecord(topic, key, value)
    }
}