package com.alexb.lab.kafka;

import com.alexb.lab.model.LabMessage;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.guava18.com.google.common.io.Files;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ProducerAvroFromFile {


    /*
     kafka-avro-console-consumer \
     --bootstrap-server localhost:9092 \
     --property print.key=true \
     --property schema.registry.url=http://localhost:8081 \
     --topic lab.avro.file
     */
    public void init() {
        KafkaProducer<Object, Object> producer = createProducer();

        File file = new File(getClass().getClassLoader().getResource("input/input-lab-v1.txt").getFile());
        try {
            List<String> allLines = Files.readLines(file, Charset.forName("UTF-8"));

            for (String line : allLines) {
                String[] parts = line.split("\\W+");

                LabMessage labMessage = LabMessage.newBuilder()
                        .setId(Integer.parseInt(parts[0]))
                        .setName(parts[1])
                        .setDescription("ProducerAvroFromFile")
                        .build();

                ProducerRecord<Object, Object> avroRecord = new ProducerRecord<>("lab.avro.file", labMessage);
                producer.send(avroRecord);
            }

        } catch (Exception e) {
            log.error("unable to send", e);
        }
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
        new ProducerAvroFromFile().init();
    }
}
