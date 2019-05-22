package com.alexb.lab.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ConsumerOffsetMng {

    public void init(String group, List<String> topics) {
        log.info("Starting Consumer");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        Jedis jedis = new Jedis("localhost", 6379);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        ConsumerRebalanceListener listener = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // nothing to do...
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                for (TopicPartition partition : partitions) {
                    try {
                        String value = jedis.hget(group, partition.topic() + ":" + partition.partition());

                        long offset = value == null ? 0 : Long.parseLong(value);
                        consumer.seek(partition, offset);
                    } catch (Exception e) {
                        log.error("Could not read offset", e);
                    }
                }
            }
        };

        try {
            consumer.subscribe(topics, listener);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("partition: {}, offset: {}, key: {}, value: {}", record.partition(), record.offset(), record.key(), record.value());

//                    if (record.value().equals("record_01")) {
//                        throw new IllegalArgumentException();
//                    }

                    jedis.hset(group, record.topic() + ":" + record.partition(), String.valueOf(record.offset() + 1));
                }
            }
        } finally {
            consumer.close();
        }
    }

    public static void main(String[] args) {
        new ConsumerOffsetMng().init("offset-mng-cg", Arrays.asList("lab.simple.3p"));
    }
}
