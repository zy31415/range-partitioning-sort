package partition

import com.typesafe.scalalogging.LazyLogging

import java.io.{PrintWriter, Writer}
import java.net.{InetAddress, Socket}
import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Try}

case class Partition(numOfServers: Int, minValue: Int = 0, maxValue: Int = 100) extends LazyLogging {

  private val range: Int = (maxValue - minValue) / numOfServers
  private val dividers: Seq[Int] = (0 until numOfServers).map(minValue + _ * range) :+ maxValue

  private val writers = mutable.HashMap.empty[Int, Writer]

  def partition(): Unit = {
    val source = Source.fromFile("/host/input/data.txt");

    // partitioning
    for (line <- source.getLines()) {
      val record = line.toInt
      val p = getPartition(record)
      logger.debug(s"Get record: $record, partitioned to $p")
      getWriter(p).write(s"$record\n")
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

  private def getPartition(record: Int): Int = {
    var partition = 1
    while (record > dividers(partition) && partition < numOfServers) {
      partition += 1
    }
    partition
  }
}
