package controllers

import common.domain.ImageHash
import common.helper.SecuredAction
import model.OfferId
import play.api.mvc.Controller
import services.ImageService

import scala.concurrent.Future

class Images(imageService: ImageService) extends Controller {

  def uploadImage = SecuredAction.async {
    Future(Ok)
  }

  def deleteImage(offerId: OfferId, imageHash: ImageHash) = SecuredAction.async {
    Future(Ok)
  }

}
