package models

import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema

data class KafkaSourceConfiguration<T>(
    val bootstrapServers: String,
    val topic: String,
    val deserializer: DeserializationSchema<T>,
)