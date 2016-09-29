package com.example.proxy

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait ProxyProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val statusFormat: RootJsonFormat[Map[String, String]] = mapFormat[String, String]
}
