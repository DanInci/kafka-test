package main

import java.util

import io.circe.Json
import org.apache.kafka.common.serialization.Deserializer
import io.circe.parser._

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
class KafkaJsonDeserializer extends Deserializer[Json] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }

  override def deserialize(topic: String, data: Array[Byte]): Json = {
    parse(data.map(_.toChar).mkString) match {
      case Left(e) => println(s"Failed to decode json $e"); Json.Null
      case Right(j) => j
    }
  }

  override def close(): Unit = { }
}
