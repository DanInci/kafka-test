package main

import java.util.Properties

import cats.effect.{Concurrent, Effect, IO, Sync}
import fs2.{Stream, StreamApp}
import io.circe.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContext

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
object ProducerApp extends StreamApp[IO] {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  override def stream(
      args            : List[String],
      requestShutdown : IO[Unit]
  ): fs2.Stream[IO, StreamApp.ExitCode] =
    for {
      producer        <- setupKafkaProducer[IO]
      producerService = new ProducerService[IO](
        producer = producer
      )
      exitCode        <- streamServer[IO](
        service  = producerService.service
      )
    } yield exitCode

  def streamServer[F[_]: Effect: Concurrent](
      service: HttpService[F],
  ): Stream[F, StreamApp.ExitCode] =
    BlazeBuilder[F]
      .bindHttp(port = 3000, host = "0.0.0.0")
      .mountService(service)
      .serve

  def setupKafkaProducer[F[_]: Sync]: Stream[F, KafkaProducer[String, Json]] = {
    val config = {
      val p = new Properties()
      p.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093")
      p
    }

    val producer = Sync[F]
      .delay(
        new KafkaProducer[String, Json](
          config,
          new StringSerializer(),
          new KafkaJsonSerializer()
        ))
    Stream.eval(producer)
  }

}
