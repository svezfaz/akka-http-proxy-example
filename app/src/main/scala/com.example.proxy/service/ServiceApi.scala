package com.example.proxy.service

import java.net.URL
import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Materializer, StreamTcpException}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.Random


trait ServiceApi {
  def status(): Future[String]
}

class Service(url: URL, timeout: FiniteDuration = 5 seconds)
             (implicit system: ActorSystem, mat: Materializer, dispatcher: ExecutionContextExecutor, poolSettings: ConnectionPoolSettings)
  extends ServiceApi with LazyLogging {

  private lazy val serviceConnectionFlow = Http().newHostConnectionPool[Long](url.getHost, url.getPort, settings = poolSettings)

  override def status(): Future[String] = serviceRequest(Get("/status")) flatMap { response =>
    val httpStatus = response.status match {
      case OK => Future.successful("Ok")
      case _ => Future.successful("Failure")
    }
    consumeEntity(response)
    val timeoutStatus = Promise[String] //to handle invalid/lengthy http client timeout handling
    system.scheduler.scheduleOnce(timeout)(timeoutStatus.success("Failure"))
    Future.firstCompletedOf(Seq(httpStatus, timeoutStatus.future))
  }

  val rand: Random = Random

  private def serviceRequest(request: HttpRequest): Future[HttpResponse] = {
    logger.debug(s"Executing [Method=${request.method.name} URI=${request.uri} Headers=${request.headers}]")
    Source.single(request -> rand.nextLong())
      .via(serviceConnectionFlow.completionTimeout(timeout))
      .runWith(Sink.head) map {
      case (scala.util.Success(res: HttpResponse), i) => res
      case (scala.util.Failure(t), _) => throw t
    } recover {
      case e: StreamTcpException => HttpResponse(StatusCodes.ServiceUnavailable)
      case e: TimeoutException => HttpResponse(StatusCodes.ServiceUnavailable)
    }
  }

  private def failAndConsume(response: HttpResponse)(e: Throwable) = {
    consumeEntity(response)
    Future.failed(e)
  }

  private def consumeEntity(response: HttpResponse) = {
    response.entity.dataBytes.runWith(Sink.ignore)
  }
}
