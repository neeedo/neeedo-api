package controllers

import common.domain.UserId
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.OfferId
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.es.EsFavoriteService

import scala.concurrent.ExecutionContext.Implicits.global

class FavoritesController(favoritesService: EsFavoriteService, securedAction: SecuredAction)
  extends Controller with ControllerUtils {

  def getFavoritesByUser(id: UserId) = securedAction.async {
    favoritesService.getFavoritesByUser(id) map {
      favorites => Ok(Json.obj("favorites" -> Json.toJson(favorites map (_.value))))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def addFavorite(userId: UserId, offerId: OfferId) = securedAction.async { implicit request =>
    favoritesService.addFavorite(userId, offerId) map {
      case Some(_) => Created(Json.toJson(offerId))
      case _ => Ok(Json.toJson(offerId))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def removeFavorite(userId: UserId, offerId: OfferId) = securedAction.async {
    favoritesService.removeFavorite(userId, offerId) map {
      case Some(_) => Ok(Json.toJson(offerId))
      case _ => NotFound
    } recover {
      case e: Exception => e.asResult
    }
  }

}
