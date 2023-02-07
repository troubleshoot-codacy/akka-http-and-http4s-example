val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.5.0-M1"

val Http4sVersion = "1.0.0-M29"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"

guardrailTasks in Compile := List(
  ScalaServer(file("./src/main/resources/api.yml"), pkg="com.codacy.generated.akka", framework = "akka-http"),
  ScalaServer(file("./src/main/resources/api.yml"), pkg="com.codacy.generated.http4s", framework = "http4s"),
)


lazy val root = (project in file("."))
  .settings(
    name := "akkahttp-vs-http4s",
    organization := "com.codacy",
    name := "http-examples",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.2.2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  )
