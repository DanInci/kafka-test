package main

import java.time.Duration
import java.util.Properties

import io.circe.Json
import io.circe.syntax._
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Produced}

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
object StreamApp extends App {

  val jsonSerde = Serdes.fromFn[Json](
    (json: Json) => json.noSpaces.getBytes,
    (bytes: Array[Byte]) => Option.apply(bytes.map(_.toChar).mkString.asJson)
  )

  implicit val consumed: Consumed[String, Json] = Consumed.`with`(Serdes.String, jsonSerde)
  implicit val produced: Produced[String, Json] = Produced.`with`(Serdes.String, jsonSerde)

  val config: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "json-filter-application")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
          "localhost:9092,localhost:9093")
    p
  }

  val builder = new StreamsBuilder()
  val jsons: KStream[String, Json] = builder.stream[String, Json]("non-filtered-json")
  val filteredJsons: KStream[String, Json] = jsons.map((k, v) => {
    println(s"Stream received: $v")
    println(s"Json object: ${v.asObject}")
    val newValue = v.mapObject(o => {
      println("ceva")
      o.filterKeys(key => key == "id" || key == "name" || key == "address")
    })
    println(s"Stream sent: $newValue")
    (k, newValue)
  })
  filteredJsons.to("filtered-json")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), config)

  streams.cleanUp()

  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

}
