#!/usr/bin/env bash

KAFKA_CONTAINER=kafka-1
prog_args="${*:-}"

if [[ -z "${prog_args}" ]]; then
    echo "Usage: $0 [topics...]"
    exit 1
fi

isKafkaContainerRunning=$(command docker inspect -f {{.State.Running}} $KAFKA_CONTAINER)
if [[ $isKafkaContainerRunning != true ]]; then
    echo "Kafka container '$KAFKA_CONTAINER' is not running"
    exit 2
fi

for arg in "$@"; do
    command docker exec -d $KAFKA_CONTAINER kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic "$arg"
done

