package ru.auto.stress.gatling

import java.io.{BufferedWriter, IOException, OutputStreamWriter, Writer}
import java.net.Socket
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

/**
  * Author: ndmelentev (ndmelentev@yandex-team.ru)
  * Created: 08.06.17
  * TODO: add logging
  */
class GraphiteWriter(prefix: String, host: String, port: Int, queueSize: Int = 1000) {
  val points: LinkedBlockingQueue[Point] = new LinkedBlockingQueue[Point](queueSize)

  {
    new Thread(new InnerWriter(), "inner-graphite-writer").start()
    print("GraphiteWriter: Started graphite writer with prefix {" + this.prefix + "} to " + host + ":" + port)
  }

  def submit(name: String, value: String, timestamp: String): Unit = {
    import java.util.concurrent.TimeUnit
    if (name == null || name.isEmpty) {
      print("GraphiteWriter: Null or empty metric name received. Ignoring it.")
    } else if (value == null || value.isEmpty) {
      print("GraphiteWriter: Null or empty metric value received. Ignoring it.")
    } else {
      try {
        val offerRes =
          if (null == timestamp) {
            points.offer(Point(name, value), 10, TimeUnit.SECONDS)
          } else {
            points.offer(Point(name, value, timestamp.toLong), 10, TimeUnit.SECONDS)
          }
        if (!offerRes) {
          print("GraphiteWriter: Unable to offer point - queue overflowed.")
        }
      } catch {
        case _: InterruptedException =>
          print("GraphiteWriter: Error while submit point. Thread interrupted.")
          Thread.currentThread.interrupt()
      }
    }
  }

  case class Point(name: String, value: String, timestamp: Long = System.currentTimeMillis() / 1000)

  private class InnerWriter() extends Runnable {

    val writesBufferSize = 4
    var writer: Option[Writer] = None
    var writesCount = 0

    override def run(): Unit = {
      try {
        writerLoopStep()
        run()
      } catch {
        case ex: InterruptedException =>
          print(s"GraphiteWriter: Intteruption catched, $ex")
          Thread.currentThread.interrupt()
        case e: Exception =>
          print(s"GraphiteWriter: Unexpected exception in main writer loop, ${e.printStackTrace()}")
          run()
      }
    }

    private def writerLoopStep() = {
      val point = nextPoint
      if (point != null) {
        if (writer.isEmpty) {
          try {
            writer = Option(newWriter)
          } catch {
            case e: IOException =>
              writer = None
              print(s"GraphiteWriter: IOException, ${e.printStackTrace()}")
          }
        }
        if (writer.isDefined) {
          writesCount += 1
          try {
            write(point)
            if (writesCount % writesBufferSize == 0) {
              print(s"GraphiteWriter: $writesBufferSize metrics will be sent to graphite")
              writesCount = 0
              writer.get.flush()
            }
          } catch {
            case _: IOException =>
              print("GraphiteWriter: Error while write point")
              try {
                writer.get.close()
                writer = None
              } catch {
                case _: IOException =>
                  print("GraphiteWriter: Error while closing writer")
              }
          }
        }
      }
      else if (writer.isDefined) if (writesCount > 0) {
        print(s"GraphiteWriter: $writesCount metrics will be sent to graphite")
        writesCount = 0
        try {
          writer.get.flush()
        } catch {
          case _: IOException =>
            print("GraphiteWriter: Error while flushing")
            writer = None
        }
      }
    }

    private def write(p: Point) = {
      if (writer.isDefined) {
        if (!prefix.isEmpty) {
          writer.get.write(prefix)
        }
        writer.get.write(sanitizeString(p.name))
        writer.get.write(' ')
        writer.get.write(p.value)
        writer.get.write(' ')
        writer.get.write(p.timestamp.toString)
        writer.get.write('\n')
      }
    }

    private def nextPoint = points.poll(10, TimeUnit.SECONDS)

    private def newWriter = {
      val socket = new Socket(host, port)
      new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
    }

    private def sanitizeString(s: String) = s.replace(' ', '-')
  }
}
