package main

import java.util

import org.apache.kafka.common.serialization.Serializer
import io.circe._


/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
class KafkaJsonSerializer extends Serializer[Json] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = { }

  override def serialize(topic: String, data: Json): Array[Byte] = {
    data.noSpaces.getBytes
  }

  override def close(): Unit = { }
}
