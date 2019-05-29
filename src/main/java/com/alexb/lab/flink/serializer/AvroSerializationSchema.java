package com.alexb.lab.flink.serializer;

import avro.shaded.com.google.common.collect.ImmutableMap;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

public class AvroSerializationSchema<T> implements KeyedSerializationSchema<T> {

    private static final long serialVersionUID = 5855129299041270015L;

    private final String topic;
    private final String schemaRegistryUrl;
    private KafkaAvroSerializer serializer;

    public AvroSerializationSchema(String topic, String schemaRegistryUrl) {
        this.topic = topic;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public byte[] serializeKey(T element) {
        return null;
    }

    @Override
    public byte[] serializeValue(T element) {
        if (serializer == null) {
            serializer = new KafkaAvroSerializer(
                    new CachedSchemaRegistryClient(schemaRegistryUrl, 100),
                    ImmutableMap.of("auto.register.schemas", "true",
                            "schema.registry.url", schemaRegistryUrl));
        }
        return serializer.serialize(topic, element);
    }

    @Override
    public String getTargetTopic(T element) {
        return topic;
    }
}
