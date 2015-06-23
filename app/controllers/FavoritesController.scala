package controllers

import common.domain.{Favorite, UserId}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.{Offer, OfferId}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.FavoriteService

import scala.concurrent.ExecutionContext.Implicits.global

class FavoritesController(favoritesService: FavoriteService, securedAction: SecuredAction)
  extends Controller with ControllerUtils {

  def getFavoritesByUser(id: UserId) = securedAction.async {
    favoritesService.getFavoritesByUser(id) map {
      favorites => Ok(Json.obj("favorites" -> Json.toJson(favorites)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def addFavorite() = securedAction.async(parse.json[Favorite]) { implicit request =>
    favoritesService.addFavorite(request.body) map {
      favorite => Created(Json.obj("favorite" -> Json.toJson(favorite)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def removeFavorite() = securedAction.async(parse.json[Favorite]) { implicit request =>
    favoritesService.removeFavorite(request.body) map {
      favorite => Ok(Json.obj("favorite" -> Json.toJson(favorite)))
    } recover {
      case e: Exception => e.asResult
    }
  }

}
