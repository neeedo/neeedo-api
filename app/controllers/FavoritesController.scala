package controllers

import common.domain.{Favorite, UserId}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.FavoriteService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class FavoritesController(favoritesService: FavoriteService, securedAction: SecuredAction)
  extends Controller with ControllerUtils {

  def getFavoritesByUser(id: UserId) = securedAction.async {
    favoritesService.getFavoritesByUser(id) map {
      favorites => Ok(Json.obj("favorites" -> Json.toJson(favorites)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def addFavorite() = securedAction.async { implicit request =>
    val tryFavorite = bindRequestJsonBody(request)(Favorite.reads)

    tryFavorite match {
      case Success(favorite) => favoritesService.addFavorite(favorite) map {
        fav => Created(Json.obj("favorite" -> Json.toJson(fav)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }

  def removeFavorite() = securedAction.async { implicit request =>
    val tryFavorite = bindRequestJsonBody(request)(Favorite.reads)

    tryFavorite match {
      case Success(favorite) => favoritesService.removeFavorite(favorite) map {
        fav => Ok(Json.obj("favorite" -> Json.toJson(fav)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }

}
