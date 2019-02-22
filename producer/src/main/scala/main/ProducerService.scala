package main

import cats.implicits._
import cats.effect.Async
import io.circe.Json
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
final class ProducerService[F[_]](
    producer: KafkaProducer[String, Json]
)(
    implicit F: Async[F]
) extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService[F] {
    case req @ POST -> Root / "kafka" =>
      for {
        json <- req.as[Json]
        record = new ProducerRecord[String, Json]("non-filtered-json", json)
        _ <- F.delay {
          producer.send(record)
          println("sent message: " + record.value)
        }
        resp <- Ok()
      } yield resp
  }

}
