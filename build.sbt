import sbt._

lazy val producer = project
  .settings(producerSettings)

lazy val stream = project
  .settings(streamSettings)

lazy val consumer = project
  .settings(consumerSettings)

lazy val simulator = project
  .settings(simulatorSettings)

def producerSettings: Seq[Setting[_]] = Seq(
  name := "Kafka Producer App",
  version := "0.1",
  scalaVersion := "2.12.8",
  mainClass := Some("main.ProducerApp"),
  libraryDependencies ++= Seq(
    `kafka-clients`,
    http4sBlazeServer,
    http4sCirce,
    http4sDSL,
    circeCore,
    circeGeneric,
    circeGenericExtras
  ),
  assemblyOutputPath in assembly := file("target/kafka-producer-app.jar"),
)

def streamSettings: Seq[Setting[_]] = Seq(
  name := "Kafka Stream App",
  version := "0.1",
  scalaVersion := "2.12.8",
  mainClass := Some("main.StreamApp"),
  libraryDependencies ++= Seq(
    `kafka-streams`,
    circeCore,
    circeGeneric,
    circeGenericExtras,
    circeParser,
    joda
  ),
  assemblyOutputPath in assembly := file("target/kafka-stream-app.jar"),
)

def consumerSettings: Seq[Setting[_]] = Seq(
  name := "Kafka Consumer App",
  version := "0.1",
  scalaVersion := "2.12.8",
  mainClass := Some("main.ConsumerApp"),
  libraryDependencies ++= Seq(
    `kafka-clients`,
    circeCore,
    circeGeneric,
    circeGenericExtras,
    circeParser,
    influxClient
  ),
  assemblyOutputPath in assembly := file("target/kafka-consumer-app.jar"),
)

def simulatorSettings: Seq[Setting[_]] = Seq(
  name := "Simulator App",
  version := "0.1",
  scalaVersion := "2.12.8",
  mainClass := Some("main.SimulatorApp"),
  libraryDependencies ++= Seq(
    akkaHttp,
    akkaActor,
    akkaJsonSupport,
    akkaStream,
    joda
  ),
  assemblyOutputPath in assembly := file("target/simulator-app.jar"),
)



lazy val akkaActor       = "com.typesafe.akka" %% "akka-actor" % "2.5.21"
lazy val akkaHttp        = "com.typesafe.akka" %% "akka-http" % "10.1.7"
lazy val akkaJsonSupport = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
lazy val akkaStream      = "com.typesafe.akka" %% "akka-stream" % "2.5.21"
lazy val joda            = "joda-time" % "joda-time" % "2.9.2"

//https://github.com/http4s/http4s
lazy val Http4sVersion = "0.18.12"

lazy val http4sBlazeServer
  : ModuleID = "org.http4s" %% "http4s-blaze-server" % Http4sVersion withSources ()
lazy val http4sCirce
  : ModuleID = "org.http4s" %% "http4s-circe" % Http4sVersion withSources ()
lazy val http4sDSL
  : ModuleID = "org.http4s" %% "http4s-dsl" % Http4sVersion withSources ()

lazy val `kafka-streams`
  : ModuleID = "org.apache.kafka" %% "kafka-streams-scala" % "2.1.1" withSources ()

lazy val `kafka-clients`
  : ModuleID = "org.apache.kafka" % "kafka-clients" % "2.1.1" withSources ()

//https://circe.github.io/circe/
lazy val circeVersion: String = "0.9.3"

lazy val circeCore: ModuleID = "io.circe" %% "circe-core" % circeVersion withSources()
lazy val circeGeneric: ModuleID = "io.circe" %% "circe-generic" % circeVersion withSources()
lazy val circeParser: ModuleID = "io.circe" %% "circe-parser" % circeVersion withSources()
lazy val circeGenericExtras: ModuleID = "io.circe" %% "circe-generic-extras" % circeVersion withSources()

//https://github.com/paulgoldbaum/scala-influxdb-client
lazy val influxClient: ModuleID = "com.paulgoldbaum" %% "scala-influxdb-client" % "0.6.1" withSources()
