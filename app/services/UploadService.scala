package services

import java.util.UUID

import common.amazon.S3Client
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future

class UploadService(s3Client: S3Client) {

  def uploadFile(file: File): Future[String] = {
    val fileHash = createFileHash(file)
    s3Client.putObject(fileHash, file).map(_ => fileHash)
  }

  def deleteFile(fileHash: String) = {
    s3Client.deleteObject(fileHash)
  }

  // Todo dummy method, implement and move elsewhere
  def createFileHash(file: File): String = UUID.randomUUID.toString

}
