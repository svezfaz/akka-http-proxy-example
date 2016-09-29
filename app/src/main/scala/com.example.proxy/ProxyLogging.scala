package com.example.proxy

import akka.event.Logging.{Info, InitializeLogger, LogLevel, LoggerInitialized}
import akka.event.LoggingAdapter
import akka.event.slf4j.Slf4jLogger
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.server.{RouteResult, _}
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.{Logger, LoggerFactory}

trait ProxyLogging extends LazyLogging {

  def logRequestResponseAndElapsedTime(level: LogLevel): Directive0 = {

    def akkaResponseTimeLoggingFunction(loggingAdapter: LoggingAdapter, requestTimestamp: Long)(req: HttpRequest)(res: RouteResult): Unit = {
      val elapsedTime: Long = (System.nanoTime - requestTimestamp) / 1000000
      val message = {
        res match {
          case Complete(resp) =>
            s"response_status=${resp.status.intValue()}"
          case Rejected(errs) =>
            s"error=$errs"
        }
      }
      loggingAdapter.log(level, s"timestamp=${System.currentTimeMillis()},method=${req.method.value},uri=${req.uri.path},elapsed_time_ms=$elapsedTime,$message")
    }

    logRequestResult(LoggingMagnet(log => {
      val requestTimestamp = System.nanoTime
      akkaResponseTimeLoggingFunction(log, requestTimestamp)
    }))
  }

}

class RequestsLogger extends Slf4jLogger {

  override lazy val log: Logger = LoggerFactory.getLogger("requestslogger")

  override def receive: Receive = {
    case Info(_, _, message: String) if message.startsWith("timestamp=") =>
      log.info(message)

    case InitializeLogger(_) =>
      sender() ! LoggerInitialized
  }
}
