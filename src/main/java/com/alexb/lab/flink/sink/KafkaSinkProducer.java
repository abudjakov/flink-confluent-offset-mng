package com.alexb.lab.flink.sink;

import com.alexb.lab.model.LabMessage;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

@Slf4j
public class KafkaSinkProducer extends RichSinkFunction<LabMessage> {

    private static final long serialVersionUID = 8734328288928200137L;

    private final String schemaRegistryUrl = "http://schema-registry:8081";

    private transient KafkaProducer<Object, Object> producer;

    @Override
    public void open(Configuration parameters) throws Exception {
        producer = createProducer();
    }

    @Override
    public void close() throws Exception {
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public void invoke(LabMessage value, Context context) throws Exception {
        ProducerRecord<Object, Object> avroRecord = new ProducerRecord<>("lab.avro.republished", value);
        producer.send(avroRecord);
        value.setDescription("republished");
        log.info("Consumed and republished: {}", value);
    }

    private KafkaProducer<Object, Object> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);

        KafkaProducer<Object, Object> producer = new KafkaProducer<>(props);
        return producer;
    }
}
