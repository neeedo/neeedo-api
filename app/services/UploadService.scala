package services

import common.amazon.S3Client
import common.domain.FileHash
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UploadService(s3Client: S3Client) {

  def uploadFile(file: File): Future[String] = {
    val fileHash = FileHash(file)
    s3Client.putObject(fileHash.value, file).map(_ => fileHash.value)
  }

  def deleteFile(fileHash: FileHash) = {
    s3Client.deleteObject(fileHash.value)
  }

}
