package ru.auto.stress.gatling

import io.gatling.commons.stats.Status
import io.gatling.http.request.ExtraInfo

import scala.collection.mutable

/**
  * Author: ndmelentev (ndmelentev@yandex-team.ru)
  * Created: 13.06.17
  */
class GatlingUtils {

  private val responseTimes: mutable.HashMap[String, mutable.HashMap[Long, Int]] = mutable.HashMap[String, mutable.HashMap[Long, Int]]()

  def getExtraInfo(extraInfo: ExtraInfo): String = {
    val responseTime = extraInfo.response.timings.responseTime

    val session = extraInfo.session.scenario
    val startDate = extraInfo.session.startDate

    val oldResponseTimes: mutable.HashMap[Long, Int] = responseTimes.getOrElse(session, mutable.HashMap.empty)
    if (oldResponseTimes.isEmpty) {
      val newResponseTimeForUid = new mutable.HashMap[Long, Int]()
      newResponseTimeForUid.update(startDate, responseTime)
      responseTimes.update(session, newResponseTimeForUid)
    } else {
      val oldResponseTimesForUid = oldResponseTimes.getOrElse(startDate, 0)
      oldResponseTimes.update(startDate, oldResponseTimesForUid + responseTime)
    }

    if (extraInfo.status.eq(Status.apply("KO"))) {
      ",URL:" + extraInfo.request.getUrl +
        " User id: " + extraInfo.request.getHeaders.get("x-uid") +
        " Request: " + extraInfo.request.getStringData +
        " Response: " + extraInfo.response.body.string
    } else {
      ""
    }
  }

  def printToGraphite(graphiteWriter: GraphiteWriter, numberOfUsers: Int, rampSeconds: Int): Unit = {

    for (scenarioTimes <- responseTimes) {

      val scenarioName = scenarioTimes._1
      val scenarioResponseTimes = scenarioTimes._2

      graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p99", percentile(99, scenarioResponseTimes.values.toSeq).toString, null)
      graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p95", percentile(95, scenarioResponseTimes.values.toSeq).toString, null)
      graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p75", percentile(75, scenarioResponseTimes.values.toSeq).toString, null)
      graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p50", percentile(50, scenarioResponseTimes.values.toSeq).toString, null)
    }

    print(s"\nWaiting 10 sec to send data to Graphit...\n")
    Thread.sleep(10000)
    print(s"Waiting complete.\n")
  }

  def percentile(p: Int, seq: Seq[Int]): Int = {
    val sorted = seq.sorted
    val k = math.ceil((sorted.size - 1) * (p / 100.0)).toInt
    if (k <= sorted.size - 1) {
      sorted(k)
    } else {
      0
    }
  }
}
