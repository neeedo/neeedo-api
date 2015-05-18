package services

import common.amazon.S3Client
import common.domain.FileHash
import java.io.File

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UploadService(s3Client: S3Client) {

  def uploadFile(file: File): Future[String] = {
    def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))

    val fileHash = FileHash(file)
    val fileExt = getExtension(file.getName)
    val newFilename = s"${fileHash.value}.$fileExt"

    s3Client.putObject(newFilename, file).map(_ => newFilename)
  }

  def deleteFile(fileHash: FileHash) = {
    s3Client.deleteObject(fileHash.value)
  }

}
