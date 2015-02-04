package controllers

import common.domain.{From, PageSize}
import model.Demand
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.MatchingService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Matching(matchingService: MatchingService) extends Controller {

  def matchDemands() = Action.async {
    matchingService.matchDemands().map {
      demands => Ok(Json.obj("demands" -> Json.toJson(demands)))
    }
  }

  //TODO receive fram and pagesize from url
  def matchDemand(from: From, pageSize: PageSize) = Action.async {
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
