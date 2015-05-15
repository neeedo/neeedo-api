package common.domain

import java.io.File
import java.util.UUID

case class FileHash(value: String) {

}

object FileHash {
  def apply(file: File) = new FileHash(createHash(file))

  // Todo implement dummy method
  def createHash(file: File): String = UUID.randomUUID.toString
}
