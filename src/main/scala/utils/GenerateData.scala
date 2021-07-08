package utils

import java.io.{File, OutputStreamWriter, PrintWriter}

object GenerateData extends App {
  val writer = new PrintWriter(new File("data.txt"))
  val r = new scala.util.Random(1)
  (0 until 100).foreach { _ =>
    writer.write(s"${r.nextInt(100)}\n")
  }

  writer.flush()
//  writer.close()
}
