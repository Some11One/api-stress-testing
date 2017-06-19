api-stress-test
=========================

Testing api with gatling 

To test it out, simply execute the following command:

    $mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.StressTest

or (if there is only one simulation):

    $mvn gatling:execute