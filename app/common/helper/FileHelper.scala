package common.helper

import play.api.Play

import scala.io.{ BufferedSource, Source }
import java.io.FileNotFoundException

object FileHelper {
  def fromClassPathFile(fileName: String): Iterator[String] = {
    val is = Play.current.classloader.getResourceAsStream(fileName)
    if (is == null) {
      throw new FileNotFoundException(fileName)
    }
    val stream: BufferedSource = Source.fromInputStream(is)
    stream.getLines()
  }

  def stringFromFile(fileName: String) = fromClassPathFile(fileName).mkString("")
}

