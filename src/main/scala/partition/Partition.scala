package partition

import com.typesafe.scalalogging.LazyLogging

import java.io.{PrintWriter, Writer}
import java.net.{InetAddress, Socket}
import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Try}

class Partition extends LazyLogging {

  private val writers = mutable.HashMap.empty[Int, Writer]

  def partition(): Unit = {
    val source = Source.fromFile("/host/input/data.txt");

    // partitioning
    for (line <- source.getLines()) {
      val record = line.toInt
      println(record)

      record match {
        case r if r < 50 =>
          getWriter(1).write(s"$record\n")
        case _ =>
          getWriter(2).write(s"$record\n")
      }
    }

    writers.foreach {case (id, w) => w.write(s"STOP node$id")}
    writers.values.foreach(_.flush())
    writers.values.foreach(_.close())
  }

  @annotation.tailrec
  private def getSocket(nodeId: Int, numTries: Int): Socket =
    Try {
      new Socket(InetAddress.getByName(s"node${nodeId}.default-subdomain.default.svc.cluster.local"), 59090)
    } match {
      case Success(value) => value
      case Failure(exception) if numTries > 1 =>
        logger.warn(s"Get socket failed with exception: $exception. Retry left: ${numTries - 1}.", exception)
        Thread.sleep(500)
        getSocket(nodeId, numTries-1)
      case Failure(exception) =>
        logger.error(s"Get socket failed with exception: ${exception.toString}. No retries. Throw the exception.", exception)
        throw exception
    }

  private def getWriter(nodeId: Int): Writer = {
    if (writers.contains(nodeId)) {
      writers(nodeId)
    } else {
      val w = new PrintWriter(getSocket(nodeId, 5).getOutputStream)
      writers(nodeId) = w
      w
    }
  }
}
