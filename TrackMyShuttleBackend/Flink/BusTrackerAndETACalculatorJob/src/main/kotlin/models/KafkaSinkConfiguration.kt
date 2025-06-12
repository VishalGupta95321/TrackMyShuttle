package models

import org.apache.flink.api.common.serialization.SerializationSchema

data class KafkaSinkConfiguration<T>(
    val bootstrapServers: String,
    val topic: String,
    val serializer: SerializationSchema<T>,
)