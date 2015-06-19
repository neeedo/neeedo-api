package services

import java.util.UUID

import common.amazon.S3Client
import common.domain.ImageId
import common.exceptions.{WrongUploadType, UploadFileToLarge}
import common.helper.ConfigLoader
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageService(s3Client: S3Client, configLoader: ConfigLoader) {

  def getImageById(id: ImageId): Future[String] = {
    s3Client.getObject(id.value)
  }

  def createImage(image: FilePart[TemporaryFile]): Future[ImageId] = {
    def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))
    val newFilename = UUID.randomUUID + getExtension(image.filename)

    image.ref.file match {
      case tooLarge if tooLarge.length() > configLoader.getInt("upload.max.filesize") =>
        Future.failed(
          new UploadFileToLarge(s"The Maximum uploadsize is ${configLoader.getInt("upload.max.filesize") / 1024 / 1024}mb"))
      case wrongType if !image.contentType.exists(s => s.contains("image")) =>
        Future.failed(
          new WrongUploadType(s"Only Image files are allowed for upload."))
      case _ =>
        s3Client.putObject(newFilename, image.ref.file).map(_ => ImageId(newFilename))
    }
  }

  def deleteFile(id: ImageId) = s3Client.deleteObject(id.value)


}
