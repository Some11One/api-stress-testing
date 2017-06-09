package ru.auto.stress.gatling

import io.gatling.commons.stats.Status
import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.ExtraInfo

import scala.collection.mutable.ListBuffer
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

  val numberOfUsers = 1000
  val rampSeconds = 10
  val scn: ScenarioBuilder = scenario("Listing").exec(Search.search)
  val httpConf: HttpProtocolBuilder = http
    .baseURL("http://autoru-api-01-sas.test.vertis.yandex.net:2600/1.0")
    .header("Accept", "application/json")
    .extraInfoExtractor(extraInfo => List(getExtraInfo(extraInfo)))
  val host = "ndmelentev-01-sas.dev.vertis.yandex.net"
  val port = 42000
  val prefix = "one_min.ndmelentev-01-sas_dev_vertis_yandex_net"
  val graphiteWriter: GraphiteWriter = new GraphiteWriter(prefix, host, port)
  var responseTimesOK: ListBuffer[Int] = ListBuffer[Int]()
  var responseTimesKO: ListBuffer[Int] = ListBuffer[Int]()


  setUp(
    scn.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds")))
  ).protocols(httpConf)

  after({
    responseTimesOK = responseTimesOK.filter(p => p != 0)
    responseTimesKO = responseTimesKO.filter(p => p != 0)
    print(s"\nOK size: ${responseTimesOK.size}")
    print(s"\n50 percentile OK: ${percentile(50, responseTimesOK)}")
    print(s"\n75 percentile OK: ${percentile(75, responseTimesOK)}")
    print(s"\n95 percentile OK: ${percentile(95, responseTimesOK)}")
    print(s"\n99 percentile OK: ${percentile(99, responseTimesOK)}")
    print(s"\nKO size: ${responseTimesKO.size}")
    print(s"\n50 percentile KO: ${percentile(50, responseTimesKO)}")
    print(s"\n75 percentile KO: ${percentile(75, responseTimesKO)}")
    print(s"\n95 percentile KO: ${percentile(95, responseTimesKO)}")
    print(s"\n99 percentile KO: ${percentile(99, responseTimesKO)}\n")
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_OK.p99", percentile(99, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_OK.p95", percentile(95, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_OK.p75", percentile(75, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_OK.p50", percentile(50, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_KO.p99", percentile(99, responseTimesKO).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_KO.p95", percentile(95, responseTimesKO).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_KO.p75", percentile(75, responseTimesKO).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.listing_response_time.result_.KO.p50", percentile(50, responseTimesKO).toString, null)
    print(s"\nWaiting 10 sec to send data to Graphit...\n")
    Thread.sleep(10000)
    print(s"Waiting complete.\n")
  })

  private def getExtraInfo(extraInfo: ExtraInfo): String = {
    if (extraInfo.status.eq(Status.apply("KO"))) {
      responseTimesKO += extraInfo.response.timings.responseTime
      ",URL:" + extraInfo.request.getUrl +
        " User id: " + extraInfo.request.getHeaders.get("x-uid") +
        " Request: " + extraInfo.request.getStringData +
        " Response: " + extraInfo.response.body.string
    } else {
      responseTimesOK += extraInfo.response.timings.responseTime
      ""
    }
  }

  private def percentile(p: Int, seq: Seq[Int]): Int = {
    val sorted = seq.sorted
    val k = math.ceil((sorted.size - 1) * (p / 100.0)).toInt
    if (k <= sorted.size - 1) {
      sorted(k)
    } else {
      0
    }
  }
}