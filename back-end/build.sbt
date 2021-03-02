name := "simple-appointment-booking"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.26.0"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.5" % "test"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion


libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion

libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.1"