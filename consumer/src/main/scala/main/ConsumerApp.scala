package main

import java.time.Duration
import java.util.Properties
import java.util.Arrays

import io.circe.Json
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer


/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
object ConsumerApp extends App {

  val config = {
    val p = new Properties()
    p.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093")
    p.put("group.id", "consumer-group-1")
    p
  }

  val consumer = new KafkaConsumer[String, Json](config, new StringDeserializer(), new KafkaJsonDeserializer())

  sys.ShutdownHookThread {
    consumer.close(Duration.ZERO)
  }

  consumer.subscribe(Arrays.asList("filtered-json"))

  while(true) {
    val records = consumer.poll(Duration.ofMillis(2000))

    records.forEach(record => {
      println("received message: " + record.value)
    })

    consumer.commitSync()
  }


}
