package controllers

import common.helper.SecuredAction
import common.helper.ImplicitConversions.ExceptionToResultConverter
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import services.UploadService

class ImagesController(uploadService: UploadService) extends Controller {

  def upload = SecuredAction.async(parse.temporaryFile) { request =>
    uploadService.uploadFile(request.body.file) map {
      fileHash => Created(Json.obj("file" -> JsString(fileHash)))
    } recover {
      case e: Exception => e.asResult
    }
  }

}
