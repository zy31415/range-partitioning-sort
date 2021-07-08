import com.typesafe.scalalogging.LazyLogging
import partition.Partition

import java.net.ServerSocket
import java.util.concurrent.CountDownLatch

object Main extends App with LazyLogging {

  val numOfServers = new CountDownLatch(2)

  val listener = new ServerSocket(59090)

  val server = new Thread(Server(listener, numOfServers))
  server.start()

  Partition.partition()

  numOfServers.await()

//  listener.close()
//  server.join()

  /* todo: time to consider to shutdown the server.

  Method:

  1. When partition is done. Send each node a STOP message.
  2. Count how many STOP messages has this node received. If it equals the number nodes. Stop the collection server and go
  to the next step.
  3. Start internal node sorting
  4. Start serve searching

  Another idea:

  Compare the performance with random search at each node vs partitioned sort.

   */

}
