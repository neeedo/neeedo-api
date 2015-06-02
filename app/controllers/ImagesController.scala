package controllers

import common.domain.ImageId
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.SecuredAction
import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponseHeaders}
import play.api.mvc.{Action, Controller}
import services.ImageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImagesController(imageService: ImageService, securedAction: SecuredAction) extends Controller {

  def getImageById(id: ImageId) = Action.async { request =>
    imageService.getImageById(id).flatMap {
      image =>
        val stream: Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = WS.url(image).getStream()
        stream.map {
          case (header, data) =>
            Ok.chunked(data).as("image/jpeg")
        }
    }.recover { case e: Exception => e.asResult }
  }

  def createImage = securedAction.async(parse.multipartFormData) { request =>
    request.body.file("image") map {
      image => {
        imageService.createImage(image).map {
          imageId => Created(Json.toJson(imageId))
        } recover {
          case e: Exception => e.asResult
        }
      }
    } getOrElse { Future(BadRequest("Missing Image")) }
  }

  def deleteImage(id: ImageId) = securedAction.async {
    imageService.deleteFile(id)
      .map { _ => Ok }
      .recover { case e: Exception =>e.asResult }
  }
}
