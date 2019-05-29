package com.alexb.lab.flink;

import com.alexb.lab.flink.serializer.AvroSerializationSchema;
import com.alexb.lab.model.LabMessage;
import com.alexb.lab.model.ResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class FlinkStreamAvroKafkaProducerSink {


    /* internal communication using bridge network */
//    private static final String BROKER = "broker:29092";
//    private static final String SCHEMA_REGISTRY = "http://schema-registry:8081";

    /* communication using exposed ports for IDE */
    private static final String BROKER = "localhost:9092";
    private static final String SCHEMA_REGISTRY = "http://localhost:8081";


    public void init() {
        log.info("-- Init --");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BROKER);
        properties.setProperty("group.id", "cg-stream-avro");
        properties.setProperty("enable.auto.commit", "false");


        FlinkKafkaConsumer<LabMessage> consumer = new FlinkKafkaConsumer<>("lab.avro", ConfluentRegistryAvroDeserializationSchema.forSpecific(LabMessage.class, SCHEMA_REGISTRY), properties);
        DataStream<ResultMessage> stream = env.addSource(consumer.setStartFromEarliest())
                .map((MapFunction<LabMessage, ResultMessage>) value -> ResultMessage.newBuilder()
                        .setId(value.getId())
                        .setName(value.getName())
                        .setStatus("Done")
                        .build())
                .returns(ResultMessage.class);

        KeyedSerializationSchema<ResultMessage> serializationSchema = new AvroSerializationSchema<>("lab.avro.sink", SCHEMA_REGISTRY);
        SinkFunction<ResultMessage> sink = new FlinkKafkaProducer<>(BROKER, "lab.avro.sink", serializationSchema);
        stream.addSink(sink).setParallelism(1).name("Kafka Sink Producer");

        try {
            log.info("-- Starting --");
            env.execute("Flink Stream Avro Kafka");
        } catch (Exception e) {
            log.error("Stream execution has failed", e);
        }


        log.info("After ...");
    }


    public static void main(String[] args) {
        System.out.println("main arguments: " + Arrays.toString(args));

        new FlinkStreamAvroKafkaProducerSink().init();
    }

}
