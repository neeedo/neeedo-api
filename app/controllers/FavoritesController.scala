package controllers

import common.domain.UserId
import common.helper.{ControllerUtils, SecuredAction}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import model.CardId
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.FavoritesService

class FavoritesController(favoritesService: FavoritesService, securedAction: SecuredAction)
  extends Controller with ControllerUtils {

  def getFavoritesByUser(id: UserId) = securedAction.async {
    favoritesService.getFavoritesByUser(id) map {
      favorites => Ok(Json.toJson(favorites))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def addFavorite(id: CardId) = securedAction.async { implicit request =>
    favoritesService.addFavorite(id) map {
      case Some(id) => Created(id)
      case _ => Ok(id)
    } recover {
      case e: Exception => e.asResult
    }
  }

  def removeFavorite(id: CardId) = securedAction.async {
    favoritesService.removeFavorite(id) map {
      case Some(id) => Ok(id)
      case _ => NotFound
    } recover {
      case e: Exception => e.asResult
    }
  }

}
