package gatling


import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

class StatusSimulation extends Simulation {

  val url = "http://localhost:9001/status"

  val getStatus: String => HttpRequestBuilder = aggPath => http("Status").get(aggPath).check(status.is(200))

  val statusScenario = scenario("MaxStatus").forever(exec(getStatus(url)))

  setUp (
    statusScenario
      .inject(atOnceUsers(3000))
      .throttle(
        reachRps(3000) in (5 seconds),
        holdFor(20 seconds)
      )
  ).disablePauses
}

