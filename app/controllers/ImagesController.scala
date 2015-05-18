package controllers

import java.util.UUID

import common.helper.SecuredAction
import common.helper.ImplicitConversions.ExceptionToResultConverter
import java.io.File
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import services.UploadService
import scala.concurrent.Future

class ImagesController(uploadService: UploadService) extends Controller {

  def upload = SecuredAction.async(parse.multipartFormData) { request =>
    request.body.file("image").map { image =>

//      if(!image.contentType.get.startsWith("image/")) //invalid contenttype
      val filename = image.filename
      val uniqueFile = new File(s"resources/${UUID.randomUUID}_$filename")
      image.ref.moveTo(uniqueFile)

      uploadService.uploadFile(uniqueFile) map {
        fileHash => Created(Json.obj("file" -> JsString(fileHash)))
      } recover {
        case e: Exception => e.asResult
      }
    } getOrElse { Future(BadRequest("Missing Image")) }
  }

}
