package com.codacy.http.examples

import cats.effect.{IO, IOApp, Resource}
import cats.implicits.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import org.http4s.server.Router
import org.http4s.dsl.Http4sDsl

import com.codacy.generated.http4s.definitions.User
import com.codacy.generated.http4s.users.{UsersHandler, UsersResource}
import org.http4s.HttpRoutes

import scala.concurrent.Future

/** Link to http4s docs: https://http4s.org/v1/docs/quickstart.html
  */
object Http4sExample extends IOApp.Simple {
  val dsl = new Http4sDsl[IO] {}
  import dsl._

  // Manually defining routes
  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }

  // Implementing Guardrail generated code
  val generatedHandler = new UsersHandler[IO] {
    override def getUser(
        respond: UsersResource.GetUserResponse.type
    )(id: String): IO[UsersResource.GetUserResponse] =
      // in case of legacy code using future, we can wrap it
      // easily with IO.fromFuture(IO(functionThatReturnsFuture()))
      IO.fromFuture(
        IO(Future.successful(UsersResource.GetUserResponse.Ok(User("some-id"))))
      )
  }

  // Joining routes
  val services =
    helloWorldService <+> new UsersResource[IO]().routes(generatedHandler)
  val httpApp = Router("/" -> services).orNotFound

  // Running server
  override val run = for
    serverFiber <- EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .useForever
      .start // forks a fiber to run in the background
    _ <- IO.println( // this runs on the main fiber
      s"Server now online. Please navigate to http://localhost:8080/hello/tester\nPress RETURN to stop..."
    )
    _ <- IO.readLine // waits until you press Enter
    _ <- serverFiber.cancel // cancel the parallel fiber
  yield ()
}
