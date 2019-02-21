import sbt._

lazy val root =
  Project(id = "kafka-test", base = file("."))
    .settings(commonSettings)

def commonSettings: Seq[Setting[_]] = Seq(
  name := "Kafka test",
  version := "0.1",
  scalaVersion := "2.12.8",
  mainClass := Some("main.WordCountExample"),
  libraryDependencies += `kafka-streams`,
  assemblyOutputPath in assembly := file("target/words-count-app.jar"),

)

lazy val `kafka-streams` : ModuleID = "org.apache.kafka" %% "kafka-streams-scala" % "2.1.1" withSources()