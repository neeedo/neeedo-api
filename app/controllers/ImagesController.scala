package controllers

import common.helper.SecuredAction
import common.helper.ImplicitConversions.ExceptionToResultConverter
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import services.UploadService
import scala.concurrent.Future

class ImagesController(uploadService: UploadService) extends Controller {

  def upload = SecuredAction.async(parse.multipartFormData) { request =>
    request.body.file("image").map { image =>

//      if(!image.contentType.get.startsWith("image/")) //invalid contenttype

      uploadService.uploadFile(image) map {
        fileHash => Created(Json.obj("file" -> JsString(fileHash)))
      } recover {
        case e: Exception => e.asResult
      }
    } getOrElse { Future(BadRequest("Missing Image")) }
  }

  def getImage(imageName: String) = SecuredAction.async { request =>
    uploadService.getFile(imageName) map {
      file => Ok.sendFile(file)
    } recover {
      case e: Exception => e.asResult
    }
  }

}
