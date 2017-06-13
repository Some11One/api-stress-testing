package ru.auto.stress.gatling

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

/**
  * Author: ndmelentev (ndmelentev@yandex-team.ru)
  * Created: 13.06.17
  */
trait GatlingSettings extends GatlingUtils {

  val numberOfUsers = 150
  val rampSeconds = 1

  val scenarioName: String
  val scn: ScenarioBuilder

  val httpConf: HttpProtocolBuilder = http
    .baseURL("http://autoru-api-01-sas.test.vertis.yandex.net:2600/1.0/user")
    .header("Accept", "application/json")
    .header("Accept", "application/protobuf")
    .extraInfoExtractor(extraInfo => List(getExtraInfo(extraInfo)))

  val graphiteHost = "ndmelentev-01-sas.dev.vertis.yandex.net"
  val graphitePort = 42000
  val graphitePrefix = "one_min.ndmelentev-01-sas_dev_vertis_yandex_net"
  val graphiteWriter: GraphiteWriter = new GraphiteWriter(graphitePrefix, graphiteHost, graphitePort)
}