package main

import java.util

import io.circe.Json
import org.apache.kafka.common.serialization.Deserializer
import io.circe.syntax._

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
class KafkaJsonDeserializer extends Deserializer[Json] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }

  override def deserialize(topic: String, data: Array[Byte]): Json = {
    data.map(_.toChar).mkString.asJson
  }

  override def close(): Unit = { }
}
