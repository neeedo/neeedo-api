package controllers

import common.domain.UserId
import common.helper.{ControllerUtils, SecuredAction}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import model.CardId
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.FavoritesService

import scala.concurrent.ExecutionContext.Implicits.global

class FavoritesController(favoritesService: FavoritesService, securedAction: SecuredAction)
  extends Controller with ControllerUtils {

  def getFavoritesByUser(id: UserId) = securedAction.async {
    favoritesService.getFavoritesByUser(id) map {
      favorites => Ok(Json.obj("favorites" -> Json.toJson(favorites map (_.value))))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def addFavorite(id: CardId) = securedAction.async { implicit request =>
    favoritesService.addFavorite(id) map {
      case Some(_) => Created(Json.obj("id" -> id.value))
      case _ => Ok(Json.obj("id" -> id.value))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def removeFavorite(id: CardId) = securedAction.async {
    favoritesService.removeFavorite(id) map {
      case Some(_) => Ok(Json.obj("id" -> id.value))
      case _ => NotFound
    } recover {
      case e: Exception => e.asResult
    }
  }

}
