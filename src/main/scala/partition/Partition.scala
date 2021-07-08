package partition

import com.typesafe.scalalogging.LazyLogging

import java.io.PrintWriter
import java.net.{InetAddress, InetSocketAddress, Socket, SocketAddress}
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Partition extends LazyLogging {

  def partition(): Unit = {
    val source = Source.fromFile("/host/input/data.txt")

    // todo: assume only 2 matches and the range is set statically.
    //   Improvement: dynamically set partition servers.
    lazy val out1 = {
      new PrintWriter(getSocket(5).getOutputStream)
    }

    lazy val out2 = {
      new PrintWriter(getSocket(5).getOutputStream)
    }

    // partitioning
    for (line <- source.getLines()) {
      val record = line.toInt
      println(record)

      record match {
        case r if r < 50 =>
          out1.write(s"$record\n")
        case _ =>
          out2.write(s"$record\n")
      }
    }

    out1.write("STOP node1")
    out2.write("STOP node2")

    out1.flush()
    out2.flush()
    out1.close()
    out2.close()
  }

  @annotation.tailrec
  private def getSocket(numTries: Int): Socket =
    Try {
      new Socket(InetAddress.getByName("localhost"), 59090)
    } match {
      case Success(value) => value
      case Failure(exception) if numTries > 1 =>
        logger.warn(s"Get socket failed with exception: $exception. Retry left: ${numTries - 1}.", exception)
        Thread.sleep(500)
        getSocket(numTries-1)
      case Failure(exception) =>
        logger.error(s"Get socket failed with exception: ${exception.toString}. No retries. Throw the exception.", exception)
        throw exception
    }
}
