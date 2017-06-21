api-stress-test
=========================

Testing api with gatling 

To launch specific file:

    $mvn gatling:execute -Dgatling.simulationClass=ru.auto.stress.gatling.StressTest

or to launch all tests sequentially ('runMultipleSimulations' configuration in pom.xml must be 'True'):

    $mvn gatling:execute