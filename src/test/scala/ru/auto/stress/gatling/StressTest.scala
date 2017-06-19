package ru.auto.stress.gatling

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._

import scala.concurrent.duration._

object OffersListingSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersListingSearch.csv").random

  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Get offers")
        .get("/offers/${category}")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    )
}

object OffersPostDeleteSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersPostSearch.csv").random
  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Add offer")
        .post("/offers/${category}")
        .body(StringBody("{\ncar_info: {\n  body_type: \"ALLROAD_5_DOORS\",\n  engine_type: \"DIESEL\",\n  transmission: \"AUTOMATIC\",\n  drive: \"ALL_WHEEL_DRIVE\",\n  mark: \"MERCEDES\",\n  model: \"GL_KLASSE\",\n  tech_param_id: 20494193\n},\ncolor_hex: \"007F00\",\nsection: USED,\navailability: IN_STOCK,\nprice_info: {\n  price: 5000.0,\n  currency: \"RUR\"\n},\ndocuments: {\n  year: 2010\n},\nstate: {\n  mileage: 50000\n},\nprivate_seller: {\n  phones: [{\n    phone: \"${phone}\"\n  }],\n  location: {\n    geobase_id: 213\n  }\n}\n}"))
        .header("Content-Type", "application/json")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
        .check(
          jsonPath("$.offer_id").saveAs("offerId")
        )
    ).pause(5)
    .exec(
      http("Delete offer")
        .delete("/offers/${category}/${offerId}".get)
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    )
}

object OffersPostUpdateDeleteSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersPostSearch.csv").random
  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Add offer")
        .post("/offers/${category}")
        .body(StringBody("{\ncar_info: {\n  body_type: \"ALLROAD_5_DOORS\",\n  engine_type: \"DIESEL\",\n  transmission: \"AUTOMATIC\",\n  drive: \"ALL_WHEEL_DRIVE\",\n  mark: \"MERCEDES\",\n  model: \"GL_KLASSE\",\n  tech_param_id: 20494193\n},\ncolor_hex: \"007F00\",\nsection: USED,\navailability: IN_STOCK,\nprice_info: {\n  price: 5000.0,\n  currency: \"RUR\"\n},\ndocuments: {\n  year: 2010\n},\nstate: {\n  mileage: 50000\n},\nprivate_seller: {\n  phones: [{\n    phone: \"${phone}\"\n  }],\n  location: {\n    geobase_id: 213\n  }\n}\n}"))
        .header("Content-Type", "application/json")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
        .check(
          jsonPath("$.offer_id").saveAs("offerId")
        )
    ).pause(30).exec(
    http("Update offer")
      .put("/offers/${category}/${offerId}")
      .body(StringBody("{\ncar_info: {\n  body_type: \"ALLROAD_5_DOORS\",\n  engine_type: \"DIESEL\",\n  transmission: \"AUTOMATIC\",\n  drive: \"ALL_WHEEL_DRIVE\",\n  mark: \"MERCEDES\",\n  model: \"GL_KLASSE\",\n  tech_param_id: 20494193\n},\ncolor_hex: \"007F00\",\nsection: USED,\navailability: IN_STOCK,\nprice_info: {\n  price: 5000.0,\n  currency: \"RUR\"\n},\ndocuments: {\n  year: 2010\n},\nstate: {\n  mileage: 50000\n},\nprivate_seller: {\n  phones: [{\n    phone: \"${phone}\"\n  }],\n  location: {\n    geobase_id: 213\n  }\n}\n}"))
      .header("Content-Type", "application/json")
      .header("x-uid", "${uid}")
      .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
      .check(status.is(200))
      .notSilent
      .asJSON
  ).pause(5).exec(
    http("Delete offer")
      .delete("/offers/${category}/${offerId}")
      .header("x-uid", "${uid}")
      .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
      .notSilent
      .check(status.is(200))
      .asJSON
  )
}

object FavoritesListingCountSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersListingSearch.csv").random

  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Get favorite offers")
        .get("/favorites/${category}")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    ).pause(5).exec(
    http("Get favorite offers count")
      .get("/favorites/${category}/count")
      .header("x-uid", "${uid}")
      .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
      .check(status.is(200))
      .notSilent
      .asJSON
  )
}

object FavoritesPostDeleteSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersListingSearch.csv").random
  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Add offer to favorites")
        .post("/favorites/${category}/1043045004-977b3")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    ).pause(5)
    .exec(
      http("Delete offer from favorites")
        .delete("/favorites/${category}/1043045004-977b3")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    )
}

object NotesListingSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersListingSearch.csv").random

  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Get notes offers")
        .get("/notes/${category}")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    ).pause(5).exec(
    http("Get notes count")
      .get("/notes/${category}/count")
      .header("x-uid", "${uid}")
      .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
      .check(status.is(200))
      .notSilent
      .asJSON
  )
}

object NotesPostDeleteSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersListingSearch.csv").random
  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Add note to offer")
        .post("/notes/${category}/1043045004-977b3")
        .body(StringBody("i really like this particular offer"))
        .header("Content-Type", "text/plain")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    ).pause(5)
    .exec(
      http("Delete note from offer")
        .delete("/notes/${category}/1043045004-977b3")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    )
}

class StressTest extends Simulation with GatlingSettings {

  val scn1: ScenarioBuilder = scenario("offers_listing").exec(OffersListingSearch.search)
  val scn2: ScenarioBuilder = scenario("offers_post_delete").exec(OffersPostDeleteSearch.search)
  val scn3: ScenarioBuilder = scenario("offers_post_update_delete").exec(OffersPostUpdateDeleteSearch.search)
  val scn4: ScenarioBuilder = scenario("favorites_listing_count").exec(FavoritesListingCountSearch.search)
  val scn5: ScenarioBuilder = scenario("favorites_post_delete").exec(FavoritesPostDeleteSearch.search)
  val scn6: ScenarioBuilder = scenario("notes_listing_count").exec(NotesListingSearch.search)
  val scn7: ScenarioBuilder = scenario("notes_post_delete").exec(NotesPostDeleteSearch.search)

  setUp(
    scn1.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds"))),
    scn2.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds"))),
    scn4.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds"))),
    scn5.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds"))),
    scn6.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds"))),
    scn7.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds")))
  ).protocols(httpConf)

  after({
    printToGraphite(graphiteWriter, numberOfUsers, rampSeconds)
  })
}