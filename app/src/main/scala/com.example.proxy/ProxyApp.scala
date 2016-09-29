package com.example.proxy

import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.example.proxy.service.Service
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.server.Directives._

import scala.concurrent.{ExecutionContextExecutor, Promise}
import scala.concurrent.duration._
import scala.util.Success

object ProxyApp extends App with ProxyApi with LazyLogging {
  implicit val system = ActorSystem("server")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit val config: Config = ConfigFactory.load()

  val url = config.getString("service.url")
  override val service = new Service(new URL(url))

  val interface = config.getString("http.interface")
  val port = config.getInt("http.port")

  val numConnections = new AtomicInteger()
  val activeRequests = new AtomicInteger()

  override lazy val routes = (get & path("status")) {
    val p = Promise[String]()
    system.scheduler.scheduleOnce(5 seconds)(p.complete(Success("OK")))
    onComplete(p.future)(complete(_))
  }

  val connectionFlow =
    Flow[HttpRequest]
      .map { req ⇒
        println(s"+ ${activeRequests.incrementAndGet()}")
        req
      }
      .via(Route.handlerFlow(routes))
      .map { res ⇒
        println(s"- ${activeRequests.decrementAndGet()}")
        res
      }
      .watchTermination() { (_, finished) ⇒
        println(s"New connection was accepted! Active: ${numConnections.incrementAndGet()}")
        finished.onComplete { _ ⇒
          println(s"Connection was completed! Active: ${numConnections.decrementAndGet()}")
        }
      }

  Http().bindAndHandle(connectionFlow, interface, port)
    .map(_ => logger.info(s"Started Proxy on $interface:$port"))

}
