import java.io.{FileOutputStream, OutputStreamWriter}

class Recorder extends java.io.Closeable {
  private val out = new FileOutputStream("/host/output/partition.txt", true)

  val writer = new OutputStreamWriter(out)

  def write(content: String): Unit = synchronized {
    writer.write(content + '\n')
    writer.flush()
  }

  override def close(): Unit = {
    out.close()
  }

}
