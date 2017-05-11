organization := "servehub"

name := "marathon-secrets-plugin"

version := "1.2.0"

scalaVersion := "2.11.11"

resolvers ++= Seq(
  "Mesosphere Public Repo" at "http://downloads.mesosphere.io/maven",
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  "mesosphere.marathon" %% "plugin-interface" % "1.4.3" % "provided",
  "org.slf4j" % "slf4j-api" % "1.7.21" % "provided",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "commons-codec" % "commons-codec" % "1.10",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.7.22" % "test"
)
