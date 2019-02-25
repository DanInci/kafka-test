package main

import java.time.Duration
import java.util.Properties

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Produced}

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
object StreamApp extends App {

  implicit val modelDecoder: Decoder[FilteredModel] = deriveDecoder[FilteredModel]
  implicit val modelEncoder: Encoder[FilteredModel] = deriveEncoder[FilteredModel]

  val jsonSerde = Serdes.fromFn[Json](
    (json: Json) => json.noSpaces.getBytes,
    (bytes: Array[Byte]) =>
      parse(bytes.map(_.toChar).mkString) match {
        case Left(e)  => println(s"Failed to parse message $e"); Option.empty
        case Right(j) => Option.apply(j)
    }
  )

  val modelSerde = Serdes.fromFn[FilteredModel](
    (model: FilteredModel) => {
      val x = model.asJson
      x.noSpaces.getBytes
    },
    (bytes: Array[Byte]) =>
      parse(bytes.map(_.toChar).mkString).map(_.as[FilteredModel]) match {
        case Right(m) if m.isRight => m.toOption
        case _  => println(s"Failed to decode model"); Option.empty
      }
  )

  implicit val consumed: Consumed[String, Json] =
    Consumed.`with`(Serdes.String, jsonSerde)
  implicit val produced: Produced[String, FilteredModel] =
    Produced.`with`(Serdes.String, modelSerde)

  val config: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "json-filter-application")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
          "localhost:9092,localhost:9093")
    p
  }

  val builder = new StreamsBuilder()
  val jsons: KStream[String, Json] = builder.stream[String, Json]("non-filtered-json")
  val maybeModels: KStream[String, Option[FilteredModel]] = jsons.map((k, v) => {
    println(s"Stream received: $v")
    (k, v.as[FilteredModel].toOption)
  })
  val models: KStream[String, FilteredModel] = maybeModels.filter((_, v) => v.isDefined).map((k, v) => {
    val model = v.get
    println(s"Stream sent: $model")
    (k, model)
  })
  models.to("filtered-json")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), config)

  streams.cleanUp()

  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

}
