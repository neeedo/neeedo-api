package services

import java.io.File

import com.amazonaws.services.s3.model.PutObjectResult
import common.amazon.S3Client
import common.domain.ImageId
import common.exceptions.{UploadFileToLarge, WrongUploadType}
import common.helper.{ConfigLoader, UUIDHelper}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.{FakeApplication, WithApplication}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ImageServiceSpec extends Specification with Mockito  {

  trait ImageServiceContext extends WithApplication {
    val file = mock[File]

    val tempFile = mock[TemporaryFile]
    tempFile.file returns file

    val filePart = mock[FilePart[TemporaryFile]]
    filePart.ref returns tempFile
    filePart.filename returns "filename.png"

    val config = Map("upload.max.filesize" -> 1024 * 1024 * 2)
    val configLoader = new ConfigLoader(Configuration.from(config))

    val imageId = ImageId("testUUID.png")

    val putObjRes = mock[PutObjectResult]

    val s3Client = mock[S3Client]
    s3Client.putObject("testUUID.png", file) returns Future(putObjRes)
    s3Client.deleteObject(imageId.value) returns Future(Unit)

    val uuid = mock[UUIDHelper]
    uuid.random returns "testUUID"


    val imageService = new ImageService(s3Client, configLoader, uuid)

    val contentTypeImage = Option("image/png")
    val contentTypeNoImage = Option("application/json")

  }

  "ImageService.getImageById" should {

    "call s3Client" in new ImageServiceContext {
      imageService.getImageById(imageId)

      there was one (s3Client).getObject(imageId.value)
    }
  }

  "ImageService.createImage" should {

    "throw UploadFileToLarge exception" in new ImageServiceContext {
      file.length() returns Integer.MAX_VALUE

      Await.result(imageService.createImage(filePart), Duration(1, "second")) must throwA[UploadFileToLarge]
    }

    "throw WrongUploadType exception" in new ImageServiceContext {
      filePart.contentType returns contentTypeNoImage

      Await.result(imageService.createImage(filePart), Duration(1, "second")) must throwA[WrongUploadType]
    }

    "return Future[ImageId]" in new ImageServiceContext {
      file.length() returns 1024
      filePart.contentType returns contentTypeImage

      Await.result(imageService.createImage(filePart), Duration(1, "second")) mustEqual ImageId("testUUID.png")
    }
  }

  "ImageService.deleteFile" should {

    "call s3Client" in new ImageServiceContext {
      imageService.deleteFile(imageId)

      there was one (s3Client).deleteObject(imageId.value)
    }
  }

}
