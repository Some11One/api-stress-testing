package ru.auto.stress.gatling.gatling

import io.gatling.commons.stats.Status
import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.ExtraInfo

import scala.concurrent.duration._

object Search {

  val feeder: RecordSeqFeederBuilder[String] = csv("search.csv").random

  val search: ChainBuilder = feed(feeder)
    .exec(http("Search").get("/user/offers/${category}")
      .header("x-uid", "${uid}")
      .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62").notSilent.asJSON
    )
}

class StressTest extends Simulation {

  val scn: ScenarioBuilder = scenario("Listing").exec(Search.search)
  val httpConf: HttpProtocolBuilder = http
    .baseURL("http://autoru-api-01-sas.test.vertis.yandex.net:2600/1.0")
    .header("Accept", "application/json")
    .check(status.is(200))
    .extraInfoExtractor(extraInfo => List(getExtraInfo(extraInfo)))

  setUp(
    scn.inject(rampUsers(1000).over(FiniteDuration.apply(10, "seconds")))
  ).protocols(httpConf)

  private def getExtraInfo(extraInfo: ExtraInfo): String = {
    if (extraInfo.status.eq(Status.apply("KO"))) {
      ",URL:" + extraInfo.request.getUrl +
        " User id: " + extraInfo.request.getHeaders.get("x-uid") +
        " Request: " + extraInfo.request.getStringData +
        " Response: " + extraInfo.response.body.string
    } else {
      ""
    }
  }
}