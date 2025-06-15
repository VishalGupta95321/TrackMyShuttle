package util.serializers

import kotlinx.serialization.json.Json
import models.BusETAData
import models.BusTrackingData
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.kafka.clients.producer.ProducerRecord

class BusETADataKafkaRecordSerializer(
    private val topic: String,
): KafkaRecordSerializationSchema<BusETAData> {

    @Transient private lateinit var json: Json

    override fun open(
        context: SerializationSchema.InitializationContext?,
        sinkContext: KafkaRecordSerializationSchema.KafkaSinkContext?
    ) {
        json = Json{ignoreUnknownKeys = true}
    }
    override fun serialize(
        element: BusETAData,
        context: KafkaRecordSerializationSchema.KafkaSinkContext,
        timestamp: Long
    ): ProducerRecord<ByteArray, ByteArray> {
        val key = element.busId.toByteArray()
        val value = json.encodeToString(element).toByteArray()

        return ProducerRecord(topic, key, value)
    }
}
