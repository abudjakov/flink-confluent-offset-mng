package com.alexb.lab.kafka;

import com.alexb.lab.model.LabMessage;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class ProducerAvroLab {


    /**
     * kafka-console-producer \
     * --broker-list localhost:9092 \
     * --property "parse.key=true" \
     * --property "key.separator= " \
     * --topic lab.simple < /Users/me/work/projects/github/flink-confluent-offset-mng/src/main/resources/input/input-lab-v1.txt
     *
     * kafka-avro-console-consumer \
     * --bootstrap-server localhost:9092 \
     * --from-beginning \
     * --property print.key=true \
     * --property schema.registry.url=http://localhost:8081 \
     * --topic lab.avro
     */
    public void init() {
        KafkaConsumer<String, String> consumer = createConsumer("avro-cg");
        consumer.subscribe(Arrays.asList("lab.simple"));

        KafkaProducer<Object, Object> producer = createProducer();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                log.info("partition: {}, offset: {}, key: {}, value: {}", record.partition(), record.offset(), record.key(), record.value());

                LabMessage labMessage = LabMessage.newBuilder()
                        .setId(Integer.parseInt(record.key()))
                        .setName(record.value())
                        .setDescription("something")
                        .build();

                ProducerRecord<Object, Object> avroRecord = new ProducerRecord<>("lab.avro", labMessage);
                producer.send(avroRecord);
            }
        }
    }

    private KafkaConsumer<String, String> createConsumer(String group) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }

    private KafkaProducer<Object, Object> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", "http://localhost:8081");

        KafkaProducer<Object, Object> producer = new KafkaProducer<>(props);
        return producer;
    }

    public static void main(String[] args) {
        new ProducerAvroLab().init();
    }
}
