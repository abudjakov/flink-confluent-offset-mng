*Run Confluent docker-compose.yml file*
```
cd docker/confuent
docker-compose up -d 
```

*Check zookeeper brokers/topics*
```
zookeeper-shell localhost:2181 ls /brokers/ids 
zookeeper-shell localhost:2181 ls /brokers/topics
```

*Create topic*
```
kafka-topics --create \
--zookeeper localhost:2181 \
--replication-factor 1 \
--partitions 2 \
--topic test    
```

*Delete topic*
```
kafka-topics --delete \
--zookeeper localhost:2181 \
--topic lab.simple
```

*Console producer with (key,value) pairs*
```
kafka-console-producer \
--broker-list localhost:9092 \
--property parse.key=true \
--property key.separator=, \
--topic lab.simple 
```

*Console consumer with (key,value) pairs*
```
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--from-beginning \
--property print.key=true \
--property key.separator=, \
--topic lab.simple 
```
-----------------------------------------------------

*Console producer with value only*
```
kafka-console-producer \
--broker-list localhost:9092 \
--topic lab.simple 
```

*Console consumer with value only*
```
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--from-beginning \
--topic lab.simple
```

-----------------------------------------------------

*Console producer that sends prepared sequences*
```
printf "record_%02g\n" {1..10} > to_send
kafka-console-producer \
--broker-list localhost:9092 \
--topic lab.simple.3p < to_send
```

*Console producer with (key value) from the file*
```
kafka-console-producer \
--broker-list localhost:9092 \
--property "parse.key=true" \
--property "key.separator= " \
--topic lab.simple < input-lab-v1.txt
```

*Kafkacat - netcat*
```
# Connect to broker -b, topic -t and read N messages -cN (use -K\t to print keys with tab delimeter)
kafkacat -b localhost:9092 -t lab.simple.3p -c2
```

*Run Redis docker-compose.yml file*
```
cd docker/redis
docker-compose up -d 
```

*Redis CLI*
```
redisLabel=`docker ps | grep redis | awk '{print $NF}'`
docker exec -it $redisLabel redis-cli

# call redis if you have above alias function in your .profile 
redis 
```
*Redis API Hash*
```
# Set the string value of a hash field
hset key field value 

# Get the value of a hash field
hget key field

# Get all the fields in a hash
hkeys key

# Get all the fields and values in a hash
hgetall key

# Get the number of fields in a hash
hlen key 
```

*Schema registry*
```
# REST API
http://localhost:8081/subjects

# subject = lab.avro-value (topic-value)

curl -i -X GET http://localhost:8081/subjects

curl -X GET http://localhost:8081/subjects/lab.avro-value/versions

curl -X GET http://localhost:8081/subjects/lab.avro-value/versions/1

curl -X GET http://localhost:8081/schemas/ids/1

curl -X GET http://localhost:8081/config/lab.avro-value
```

*Create consumer with schema registry*
```
kafka-avro-console-consumer \
--bootstrap-server localhost:9092 \
--from-beginning \
--property print.key=true \
--property schema.registry.url=http://localhost:8081 \
--topic lab.avro.sink
```

*Run Flink job on session cluster*
```
JOBMANAGER_CONTAINER=$(docker ps --filter name=jobmanager --format={{.ID}})
$ docker cp path/to/jar "$JOBMANAGER_CONTAINER":/job.jar
$ docker exec -it "$JOBMANAGER_CONTAINER" flink run -c a.b.RunClass /job.jar

JOBMANAGER_CONTAINER=$(docker ps --filter name=jobmanager --format={{.ID}})
docker cp flink-confluent-offset-mng-1.0.0.0-SNAPSHOT.jar "$JOBMANAGER_CONTAINER":/job.jar
docker exec -it "$JOBMANAGER_CONTAINER" flink run -sae -c com.alexb.lab.flink.FlinkStreamAvroKafkaProducerSink /job.jar
```

*Scale the cluster up or down to N TaskManagers*
```
docker-compose scale taskmanager=<N>
```

*Netcat scanning host and port*
```
nc -vz schema-registry 8081
```