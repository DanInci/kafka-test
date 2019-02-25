package main

import java.time.Duration
import java.util.Properties
import java.util.Arrays

import com.paulgoldbaum.influxdbclient.{InfluxDB, Point}
import io.circe._
import io.circe.generic.semiauto.deriveDecoder
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContext

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
object ConsumerApp extends App {

  implicit val modelDecoder: Decoder[FilteredModel] =
    deriveDecoder[FilteredModel]

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val influxdb = InfluxDB.connect(
    host = "localhost",
    port = 8086,
    username = "influxuser",
    password = "qwerty"
  )
  val db = influxdb.selectDatabase(databaseName = "filtered_models")

  sys.addShutdownHook(influxdb.close())

  val config = {
    val p = new Properties()
    p.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
          "localhost:9092,localhost:9093")
    p.put("group.id", "consumer-group-1")
    p
  }

  val consumer = new KafkaConsumer[String, Json](config,
                                                 new StringDeserializer(),
                                                 new KafkaJsonDeserializer())

  consumer.subscribe(Arrays.asList("filtered-json"))

  while (true) {
    val records = consumer.poll(Duration.ofMillis(2000))

    records.forEach(record => {
      record.value.as[FilteredModel] match {
        case Left(e) => println(s"Failed to decode model $e")
        case Right(m) => {
          println(s"Consumer received: $m")
          val point = Point("consumer_data")
            .addField("id", m.id)
            .addField("name", m.name)
            .addField("address", m.address)
          db.write(point)
            .onComplete(result =>
              if (result.isFailure) {
                println("Failed to insert data into influx")
            })
        }
      }
    })

    consumer.commitSync()
  }

}
