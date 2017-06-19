package ru.auto.stress.gatling

import io.gatling.commons.stats.Status
import io.gatling.http.request.ExtraInfo

import scala.collection.mutable

/**
  * Author: ndmelentev (ndmelentev@yandex-team.ru)
  * Created: 13.06.17
  */
trait GatlingUtils {

  private val responseTimes: mutable.HashMap[Int, Int] = mutable.HashMap[Int, Int]()

  def getExtraInfo(numberOfSteps: Int, extraInfo: ExtraInfo): String = {

    val responseTime = extraInfo.response.timings.responseTime
    val uid = extraInfo.request.getHeaders.get("uid")
    val oldResponseTimeForUid = responseTimes.getOrElse(uid.toInt, 0)
    responseTimes.update(uid.toInt, oldResponseTimeForUid + responseTime)

    if (extraInfo.status.eq(Status.apply("KO"))) {
      ",URL:" + extraInfo.request.getUrl +
        " User id: " + extraInfo.request.getHeaders.get("x-uid") +
        " Request: " + extraInfo.request.getStringData +
        " Response: " + extraInfo.response.body.string
    } else {
      ""
    }
  }

  def printToGraphit(graphiteWriter: GraphiteWriter, numberOfUsers: Int, rampSeconds: Int, scenarioName: String): Unit = {
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p99", percentile(99, responseTimes.values.toSeq).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p95", percentile(95, responseTimes.values.toSeq).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p75", percentile(75, responseTimes.values.toSeq).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p50", percentile(50, responseTimes.values.toSeq).toString, null)
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
