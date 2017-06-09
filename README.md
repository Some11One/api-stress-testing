api-stress-test
=========================

Testing api with gatling 

To test it out, simply execute the following command:

    $mvn gatling:execute -Dgatling.simulationClass=gatling.StressTest

or simply (if there is only one simulation):

    $mvn gatling:execute

Listing stress test (fires 1000 users in 10 seconds every day at 00:00:00)

    https://grafana.yandex-team.ru/dashboard/db/api-stress-test?orgId=1&from=1497026451679&to=1497027027186&refresh=1m
