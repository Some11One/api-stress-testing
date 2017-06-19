#!/usr/bin/env bash

# listing
mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.tests.OffersListingTest

# post + delete
mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.tests.OffersPostDeleteTest

# post + update + delete
mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.tests.OffersPostUpdateDeleteTest
