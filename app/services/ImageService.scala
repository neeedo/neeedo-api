package services

import java.io.{FileOutputStream, File}
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.UUID

import common.amazon.S3Client
import common.domain.ImageId
import common.exceptions.{WrongUploadType, UploadFileToLarge}
import common.helper.ConfigLoader
import play.api.libs.Files.TemporaryFile
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.ws.{WSResponseHeaders, WSRequestHolder, WS}
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Play.current
import play.api.http.HeaderNames._

class ImageService(s3Client: S3Client, configLoader: ConfigLoader) {

  def getImageById(id: ImageId): Future[String] = {
    s3Client.getObject(id.value)
  }

  private def getExtension(filename: String) = filename.substring(filename.lastIndexOf("."))

  def createImage(image: FilePart[TemporaryFile]): Future[ImageId] = {

    image.ref.file match {
      case tooLarge if tooLarge.length() > configLoader.getInt("upload.max.filesize") =>
        Future.failed(
          new UploadFileToLarge(s"The Maximum uploadsize is ${configLoader.getInt("upload.max.filesize") / 1024 / 1024}mb"))
      case wrongType if !image.contentType.exists(s => s.contains("image")) =>
        Future.failed(
          new WrongUploadType(s"Only Image files are allowed for upload."))
      case _ =>
        val newFilename = UUID.randomUUID + getExtension(image.filename)
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


}
