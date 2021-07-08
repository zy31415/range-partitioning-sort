import com.typesafe.scalalogging.LazyLogging

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{ServerSocket, SocketException}
import java.util.concurrent.CountDownLatch
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Using}

case class Server(serverSocket: ServerSocket, numOfServer: CountDownLatch) extends Runnable with LazyLogging {

  override def run(): Unit = {

    // Use try with resources pattern to make sure the resource is closed.
    Using.Manager { use =>

      val recorder = use(new Recorder)

      val PATTERN = "STOP\\s+(.*)".r

      @tailrec
      def process(reader: BufferedReader): Unit = reader.readLine() match {
          case content if content == null =>
            logger.info("End of stream.")
          case PATTERN(node) =>
            logger.info(s"End of partitioning of node ${node}.")
            numOfServer.countDown()
            logger.info(s"Number of servers: ${numOfServer.getCount}")
          case content =>
            logger.info(s"Got record: ${content}")
            // note that this method is synchronized so that no more than 1 thread can write at the same time.
            recorder.write(content)
            // continue to read the stream
            process(reader)
        }

      logger.info("Server is running ...")

      // server keep running
      while(true) {
//      while(numOfServer.getCount > 0) {

        // Don't use try with resources pattern since the resource is closed in a different thread.
        try {
          val connection = serverSocket.accept()
          logger.info(s"Accepted an connection from ${connection.getInetAddress.toString}:${connection.getPort}.")

          Future {
            // todo: this is blocking IO. How I use nio to rewrite this?
            val reader = new BufferedReader(new InputStreamReader(connection.getInputStream))

            process(reader)

            logger.info(s"Connection will be closed: ${connection.getInetAddress.toString}:${connection.getPort}.")
            connection.shutdownInput()
            connection.close()
          }
        } catch {
          case e: IOException =>
            logger.error("Got IOException while handling a connection.", e)
          case e: SocketException =>
            logger.warn("Socket is closed.")
          case e =>
            logger.error("Got exception while handling a connection.", e)
        }
      }
    } match {
      case Failure(exception) =>
        logger.error("Got exception while creating server socket. ", exception)
      case Success(_) =>
    }
  }

  logger.info("Collection server ends. Not active servers.")

}
