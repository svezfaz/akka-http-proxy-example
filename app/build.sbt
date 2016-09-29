name         := "akka-http-proxy-example"
version      := "1.0"
scalaVersion := "2.11.8"

val akkaV       = "2.4.10"

val commonDeps = Seq(
  "com.typesafe.scala-logging" %% "scala-logging"               % "3.1.0",
  "org.slf4j"                  %  "slf4j-api"                   % "1.7.12"   % Runtime,
  "ch.qos.logback"             %  "logback-classic"             % "1.0.9"    % Runtime,
  "org.codehaus.janino"        %  "janino"                      % "2.7.8"    % Runtime,
  "org.scalatest"              %% "scalatest"                   % "2.2.4"    % Test,
  "org.scalamock"              %% "scalamock-scalatest-support" % "3.2.2"    % Test
)

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-slf4j"                        % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental"            % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-testkit"                      % akkaV % Test,
  "com.typesafe.akka" %% "akka-http-testkit"                 % akkaV % Test
)

libraryDependencies ++= commonDeps ++ akkaDeps
