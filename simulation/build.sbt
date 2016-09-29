enablePlugins(GatlingPlugin)

name         := "akka-http-proxy-example-simulation"
version      := "1.0"
scalaVersion := "2.11.8"

val akkaV       = "2.4.10"
val gatlingV    = "2.1.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-actor"               % akkaV,
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingV % Test exclude("com.typesafe.akka", "akka-actor_2.11"),
  "io.gatling"            % "gatling-test-framework"    % gatlingV % Test exclude("com.typesafe.akka", "akka-actor_2.11")
)
