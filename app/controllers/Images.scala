package controllers

import common.helper.SecuredAction
import common.helper.ImplicitConversions.ExceptionToResultConverter
import model.OfferId
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.UploadService

class Images(uploadService: UploadService) extends Controller {

  def upload = SecuredAction.async(parse.temporaryFile) { request =>
    uploadService.uploadFile(request.body.file) map {
      fileHash => Created(Json.obj("file" -> JsString(fileHash)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def deleteImage(offerId: OfferId, imageHash: String) = SecuredAction.async {
    Future(Ok)
  }

}
