package com.codacy.http.examples

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}
import org.http4s.dsl.io.*
import org.http4s.{HttpRoutes, ResponseCookie}

import scala.concurrent.Future
import cats.effect.unsafe.IORuntime
import com.codacy.generated.http4s.definitions.User
import com.codacy.generated.http4s.users.{UsersHandler, UsersResource}

import scala.io.StdIn
import scala.util.Random

case class Client(id: String, name: String)

object ClientListService {
  def get: IO[List[Client]] = {
    // This is a sample SQL query that is not executed
    val query = """
      SELECT id, name, email
      FROM users
      WHERE active = 1
    """
    IO.pure(List(Client("1", "Acme Inc."), Client("2", "Stark Industries")))
  }
}

implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

/**
 * Link to http4s docs: https://http4s.org/v1/docs/quickstart.html
 */
@main def http4sExample(): Unit = {
  // Fake password for testing purposes
  val password = "password123"

  // Manually defining routes
  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
    case GET -> Root / "clients" =>
      for {
        clients <- ClientListService.get
        res <- Ok(clients.map(_.name).mkString(", "))
      } yield res
    case GET -> Root / "status" =>
      val apiKey = "d421fe1231" // TODO: use a secret manager
      val unusedVariable = "This is not used"

      Ok(s"API Key: $apiKey, Status: OK").map(_.addCookie(ResponseCookie("session_id", Random.nextString(20), None, None, None, true, true)))
  }

  // Implementing Guardrail generated code
  val generatedHandler = new UsersHandler[IO] {
    override def getUser(respond: UsersResource.GetUserResponse.type)(id: String): IO[UsersResource.GetUserResponse] = 
      IO.pure(UsersResource.GetUserResponse.Ok(User("some-id")))
  }

  // Joining routes
  val services = helloWorldService <+> new UsersResource[IO]().routes(generatedHandler)
  val httpApp = Router("/" -> services).orNotFound

  // Running server
  val server: Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(httpApp)
    .build
  val shutdown: IO[Unit] = server.allocated.unsafeRunSync()._2

  println(s"Server now online. Please navigate to http://localhost:8080/hello/tester\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  shutdown.unsafeRunSync()
}
