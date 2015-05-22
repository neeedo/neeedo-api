package services

import java.util.UUID

import common.amazon.S3Client
import common.domain.ImageId
import java.io.File

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageService(s3Client: S3Client) {

  def getImageById(id: ImageId): Future[File] = {
    val image = new File(s"resources/${id.value}")
    s3Client.getObject(id.value, image) map { _ => image }
  }

  def createImage(image: FilePart[TemporaryFile]): Future[String] = {
    def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))

    val newFilename = UUID.randomUUID + getExtension(image.filename)
    val newFile = new File(s"resources/$newFilename")
    image.ref.moveTo(newFile)

    s3Client.putObject(newFilename, newFile).map(_ => newFilename)
  }

  def deleteFile(id: ImageId) = s3Client.deleteObject(id.value)
}
