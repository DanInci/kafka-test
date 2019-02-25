package main


import akka.actor.{ActorRefFactory, ActorSystem}
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import org.joda.time.{LocalDateTime, _}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, deserializationError}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 22/02/2019
  */
object SimulatorApp extends App with SprayJsonSupport with DefaultJsonProtocol {

  lazy val system = ActorSystem.create("DataSimulator")
  implicit  lazy val executionContext: ExecutionContext = system.dispatcher
  implicit  lazy val actorRefFactory: ActorRefFactory = system
  implicit       val materializer: ActorMaterializer = ActorMaterializer()

  val http = Http(system)


  implicit val jodaLocalDateTimeFormat = new RootJsonFormat[LocalDateTime] {
    def write(value: LocalDateTime) = JsString(value.toString())
    def read(value: JsValue): LocalDateTime = value match {
      case JsString(s) =>
        try LocalDateTime.parse(s)
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a date-time")
    }
  }
  implicit val unfilteredFormat: RootJsonFormat[UnfilteredModel] = jsonFormat5(UnfilteredModel)


  def generateData = {
    val NOW = LocalDateTime.now(DateTimeZone.UTC)
    val rawReading = UnfilteredModel(
                        id           = NOW.getSecondOfMinute.toString,
                        randomData   = NOW.dayOfMonth().get().toString,
                        name         = "lulu" + NOW.dayOfYear().get().toString,
                        timestamp    = NOW.toString("YYYY-MM-dd HH:mm:ss"),
                        anotherRData = NOW.getMillisOfSecond
    )
    this.sendRequest(rawReading).flatMap(_ => Future.successful(rawReading))
  }

//  def sendRequestTest(rawReading: UnfilteredModel) = Future {
//    println(s"Send message to Producer: ${rawReading}")
//  }

  def sendRequest(reading: UnfilteredModel) = Future {
    Marshal(reading).to[RequestEntity].flatMap { entity =>
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://localhost:3000/kafka", entity = entity)
      http.singleRequest(request).map(e => e.entity.discardBytes())
    }
  }.recoverWith {
    case e: Exception =>
      Future.successful(println(s"Could not send upp simulator message $reading " + "error: " + e))
  }


  final case class UnfilteredModel(id           : String,
                                   randomData   : String,
                                   name         : String,
                                   timestamp    : String,
                                   anotherRData : Int
                                  )
  while(true) {

    generateData.flatMap(msg => Future.successful(println(s"Message sent to producer!")))
    Thread.sleep(1000)

  }

}
