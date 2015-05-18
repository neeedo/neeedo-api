package services

import java.util.UUID

import com.amazonaws.services.s3.model.{ObjectMetadata, S3Object}
import common.amazon.S3Client
import common.domain.FileHash
import java.io.File

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UploadService(s3Client: S3Client) {

  def getFile(filename: String): Future[File] = {
    val file = new File(s"resources/$filename")
    s3Client.getObject(filename, file) map {
      _ => file
    }
  }

  def uploadFile(image: FilePart[TemporaryFile]): Future[String] = {
    def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))

    val filename = image.filename
    val uniqueFile = new File(s"resources/${UUID.randomUUID}_$filename")
    image.ref.moveTo(uniqueFile)

    val fileHash = FileHash(uniqueFile)
    val fileExt = getExtension(uniqueFile.getName)
    val newFilename = fileHash.value + fileExt

    val res: Future[String] = s3Client.putObject(newFilename, uniqueFile).map(_ => newFilename)
    uniqueFile.delete()
    res
  }

  def deleteFile(fileHash: FileHash) = {
    s3Client.deleteObject(fileHash.value)
  }

}
