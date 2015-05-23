package services

import java.util.UUID

import common.amazon.S3Client
import common.domain.ImageId

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageService(s3Client: S3Client) {

  def getImageById(id: ImageId): Future[String] = {
    s3Client.getObject(id.value)
  }

  def createImage(image: FilePart[TemporaryFile]): Future[ImageId] = {
    def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))
    val newFilename = UUID.randomUUID + getExtension(image.filename)
    s3Client.putObject(newFilename, image.ref.file).map(_ => ImageId(newFilename))
  }

  def deleteFile(id: ImageId) = s3Client.deleteObject(id.value)
}
