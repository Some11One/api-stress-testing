#!/usr/bin/env bash

# listing
mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.tests.user.OffersListingTest

# post + delete
mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.tests.user.OffersPostDeleteTest

