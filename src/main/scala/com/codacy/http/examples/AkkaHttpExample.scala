package com.codacy.http.examples

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import com.codacy.generated.akka.users.{UsersHandler, UsersResource}
import com.codacy.generated.akka.definitions.User
import com.codacy.generated.akka.users.UsersResource

import scala.concurrent.Future
import scala.io.StdIn

/**
 * Link to akka-http docs: https://doc.akka.io/docs/akka-http/current/introduction.html
 */
@main def akkaHttpExample(): Unit = {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext = system.executionContext

  // Manually defining routes
  val route =
    get {
      path("hello" / Segment) { name =>
        complete(s"Hello, $name.")
      }
    }

  // Implementing Guardrail generated code
  val generatedHandler = new UsersHandler {
    override def getUser(respond: UsersResource.GetUserResponse.type)(id: String): Future[UsersResource.GetUserResponse] =
      Future.successful(UsersResource.GetUserResponse.OK(User("some-id")))
  }

  // Joining routes & running server
  val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8080)
    .bind(route ~ UsersResource.routes(generatedHandler))

  println(s"Server now online. Please navigate to http://localhost:8080/hello/tester\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
