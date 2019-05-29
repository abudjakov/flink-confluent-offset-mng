package com.alexb.lab.flink;

import com.alexb.lab.flink.sink.KafkaSinkProducer;
import com.alexb.lab.model.LabMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class FlinkStreamAvroKafkaAndRepublish {


    final String schemaRegistryUrl = "http://schema-registry:8081";

    public void init(String topic) {
        log.info("-- Init --");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:29092");
        properties.setProperty("group.id", "cg-stream-avro");
        properties.setProperty("enable.auto.commit", "false");


        FlinkKafkaConsumer<LabMessage> consumer = new FlinkKafkaConsumer<>(topic, ConfluentRegistryAvroDeserializationSchema.forSpecific(LabMessage.class, schemaRegistryUrl), properties);
        DataStream<LabMessage> stream = env.addSource(consumer.setStartFromEarliest());

        stream.addSink(new KafkaSinkProducer()).setParallelism(1).name("Kafka Sink Producer");

        try {
            log.info("-- Starting --");
            env.execute("Flink Stream Avro Kafka");
        }
        catch (Exception e) {
            log.error("Stream execution has failed", e);
        }


        log.info("After ...");
    }




    public static void main(String[] args) {
        System.out.println("main arguments: " + Arrays.toString(args));

        new FlinkStreamAvroKafkaAndRepublish().init("lab.avro");
    }

}
