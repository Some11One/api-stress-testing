package ru.auto.stress.gatling.tests.user

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import ru.auto.stress.gatling.GatlingSettings

import scala.concurrent.duration._

object OffersPostDeleteSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("postSearch.csv").random
  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Add offer")
        .post("/offers/${category}")
        .body(StringBody("{\ncar_info: {\n  body_type: \"ALLROAD_5_DOORS\",\n  engine_type: \"DIESEL\",\n  transmission: \"AUTOMATIC\",\n  drive: \"ALL_WHEEL_DRIVE\",\n  mark: \"MERCEDES\",\n  model: \"GL_KLASSE\",\n  tech_param_id: 20494193\n},\ncolor_hex: \"007F00\",\nsection: USED,\navailability: IN_STOCK,\nprice_info: {\n  price: 5000.0,\n  currency: \"RUR\"\n},\ndocuments: {\n  year: 2010\n},\nstate: {\n  mileage: 50000\n},\nprivate_seller: {\n  phones: [{\n    phone: \"${phone}\"\n  }],\n  location: {\n    geobase_id: 213\n  }\n}\n}"))
        .header("Content-Type", "application/json")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .notSilent
        .asJSON
        .check(
          jsonPath("$.offer_id").saveAs("offerId")
        )
    ).pause(2)
    .exec(
      http("Delete offer")
        .delete("/offers/${category}/${offerId}".get)
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .notSilent
        .asJSON
    )
  var offerId = "-1"
  var category = ""
}

class OffersPostDeleteTest extends Simulation with GatlingSettings {

  override val scn: ScenarioBuilder = scenario("PostDelete").exec(OffersPostDeleteSearch.search)
  override val scenarioName: String = "delete_response"

  setUp(
    scn.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds")))
  ).protocols(httpConf)

  after({
    printToGraphit(graphiteWriter, numberOfUsers, rampSeconds, scenarioName)
  })
}