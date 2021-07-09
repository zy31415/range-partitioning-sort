import java.io.{FileOutputStream, OutputStreamWriter}

class Recorder extends java.io.Closeable {

  private val localHostname = java.net.InetAddress.getLocalHost.getHostName
  private val out = new FileOutputStream(s"/host/output/partition_${localHostname}.txt", true)

  val writer = new OutputStreamWriter(out)

  def write(content: String): Unit = synchronized {
    writer.write(content + '\n')
    writer.flush()
  }

  override def close(): Unit = {
    out.close()
  }

}
