package models

import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema

data class KafkaSinkConfiguration<T>(
    val bootstrapServers: String,
    val serializer: KafkaRecordSerializationSchema<T>,
)