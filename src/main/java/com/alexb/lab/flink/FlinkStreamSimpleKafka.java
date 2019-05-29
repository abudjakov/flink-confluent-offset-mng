package com.alexb.lab.flink;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class FlinkStreamSimpleKafka {


    public void init(String topic) {
        log.info("-- Init --");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:29092");
        properties.setProperty("group.id", "cg-stream-simple");
        properties.setProperty("enable.auto.commit", "false");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(topic, new SimpleStringSchema(), properties);
        DataStream<String> stream = env.addSource(consumer.setStartFromEarliest());

        stream.addSink(new SinkFunction<String>() {
            @Override
            public void invoke(String value, Context context) throws Exception {
                log.info("Consumed: {}", value);
            }
        }).setParallelism(1).name("Logger Sink");

        try {
            log.info("-- Starting --");
            env.execute("Flink Stream Simple Kafka");
        }
        catch (Exception e) {
            log.error("Stream execution has failed", e);
        }


        log.info("After ...");
    }


    public static void main(String[] args) {
        System.out.println("main arguments: " + Arrays.toString(args));

        new FlinkStreamSimpleKafka().init("lab.simple");
    }

}
