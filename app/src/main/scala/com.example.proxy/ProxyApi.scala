package com.example.proxy

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.CacheControl
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.CacheDirectives.`no-cache`
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.example.proxy.service.ServiceApi
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

trait ProxyApi extends ProxyProtocol with ProxyLogging {
  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def service: ServiceApi

  private val statusRoute: Route = (path("status") & get) {
    onComplete(service.status()) {
      case Success(status) => complete {
        StatusCodes.OK -> Map[String, String](
          "status" -> status.toString
        ).toJson
      }
      case Failure(e) => failWith(e)
    }
  }

  lazy val routes = respondWithHeaders(CacheControl.create(`no-cache`), RawHeader("Pragma", "no-store")) {
    statusRoute
  }
}
