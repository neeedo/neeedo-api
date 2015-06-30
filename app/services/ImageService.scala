package services

import java.io.{File, FileOutputStream}
import java.util.UUID

import common.amazon.S3Client
import common.domain.ImageId
import common.exceptions.{UploadFileToLarge, WrongUploadType}
import common.helper.{ConfigLoader, UUIDHelper}
import play.api.Play.current
import play.api.http.HeaderNames._
import play.api.libs.Files.TemporaryFile
import play.api.libs.iteratee.Iteratee
import play.api.libs.ws.WS
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImageService(s3Client: S3Client, configLoader: ConfigLoader, uuid: UUIDHelper) {

  def getImageById(id: ImageId): Future[String] = {
    s3Client.getObject(id.value)
  }

  def createImage(image: FilePart[TemporaryFile]): Future[ImageId] = {

    image.ref.file match {
      case tooLarge if tooLarge.length() > configLoader.getInt("upload.max.filesize") =>
        Future.failed(
          new UploadFileToLarge(s"The Maximum uploadsize is ${configLoader.getInt("upload.max.filesize") / 1024 / 1024}mb"))
      case wrongType if !image.contentType.exists(s => s.contains("image")) =>
        Future.failed(
          new WrongUploadType(s"Only Image files are allowed for upload."))
      case _ =>
        val newFilename = uuid.random + getExtension(image.filename)
        s3Client.putObject(newFilename, image.ref.file).map(_ => ImageId(newFilename))
    }
  }

  def createImage(url: String): Future[ImageId] = {
    WS.url(url).getStream().flatMap {
      case (headers, stream) if headers.headers.get(CONTENT_LENGTH).get.head.toInt > configLoader.getInt("upload.max.filesize") =>
        throw new UploadFileToLarge(s"The Maximum uploadsize is ${configLoader.getInt("upload.max.filesize") / 1024 / 1024}mb")
      case (headers, stream) if !headers.headers.get(CONTENT_TYPE).get.head.contains("image") =>
        throw new WrongUploadType(s"Only Image files are allowed for upload.")
      case (headers, stream) =>
        val newFilename = UUID.randomUUID + getExtension(url)
        val tempFile = File.createTempFile(newFilename, null, new File("/Users/Stefan/"))

        val outputStream = new FileOutputStream(tempFile)

        val iteratee = Iteratee.foreach[Array[Byte]] { bytes =>
          outputStream.write(bytes)
        }

        (stream |>>> iteratee).andThen {
          case result =>
            outputStream.close()
            result.get
        }.flatMap(_ => s3Client.putObject(newFilename, tempFile).map(_ => ImageId(newFilename)))
    }
  }

  def deleteFile(id: ImageId) = s3Client.deleteObject(id.value)

  private def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))
}
