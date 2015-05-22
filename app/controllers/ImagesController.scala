package controllers

import common.domain.ImageId
import common.helper.SecuredAction
import common.helper.ImplicitConversions.ExceptionToResultConverter
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import services.ImageService
import scala.concurrent.Future

class ImagesController(imageService: ImageService) extends Controller {

  def getImageById(id: ImageId) = SecuredAction.async { request =>
    imageService.getImageById(id)
      .map { image => Ok.sendFile(image) }
      .recover { case e: Exception => e.asResult }
  }

  def createImage = SecuredAction.async(parse.multipartFormData) { request =>
    request.body.file("image") map {
      (image) => {
//      if(!image.contentType.get.startsWith("image/")) //invalid contenttype
        imageService.createImage(image)
          .map { id => Created(Json.obj("image" -> JsString(id))) }
          .recover { case e: Exception => e.asResult }
      }
    } getOrElse { Future(BadRequest("Missing Image")) }
  }

  def deleteImage(id: ImageId) = SecuredAction.async {
    imageService.deleteFile(id)
      .map { _ => Ok }
      .recover { case e: Exception =>e.asResult }
  }
}
