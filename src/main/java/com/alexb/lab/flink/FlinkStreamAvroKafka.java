package com.alexb.lab.flink;

import com.alexb.lab.model.LabMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class FlinkStreamAvroKafka {


    public void init(String topic) {
        log.info("-- Init --");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:29092");
        properties.setProperty("group.id", "cg-stream-avro");
        properties.setProperty("enable.auto.commit", "false");

        String schemaRegistryUrl = "http://schema-registry:8081";

        FlinkKafkaConsumer<LabMessage> consumer = new FlinkKafkaConsumer<>(topic, ConfluentRegistryAvroDeserializationSchema.forSpecific(LabMessage.class, schemaRegistryUrl), properties);
        DataStream<LabMessage> stream = env.addSource(consumer.setStartFromEarliest());

        stream.addSink(new SinkFunction<LabMessage>() {
            @Override
            public void invoke(LabMessage value, Context context) throws Exception {
                log.info("Consumed: {}", value);
            }
        }).setParallelism(1).name("Logger Sink");

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

        new FlinkStreamAvroKafka().init("lab.avro");
    }

}
