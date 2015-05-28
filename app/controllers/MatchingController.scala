package controllers

import common.domain.{From, PageSize}
import common.helper.SecuredAction
import model.Demand
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.MatchingService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchingController(matchingService: MatchingService, securedAction: SecuredAction) extends Controller {

  /* TODO diese methode noch umbauen in ListDemands bzw ListOffers als Schnupperaction
   * TODO für nicht registrierte (In den Login / die Registrierung treiben */
  def matchDemands() = Action.async {
    matchingService.matchDemands().map {
      demands => Ok(Json.obj("demands" -> Json.toJson(demands)))
    }
  }

  def matchDemand(from: From, pageSize: PageSize) = securedAction.async {
    implicit request => request.body.asJson match {
      case Some(json) => json.asOpt[Demand] match {
        case Some(demand) =>
          matchingService.matchDemand(from, pageSize, demand).map {
            result => Ok(Json.toJson(result))
          }
        case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
      }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }
}
