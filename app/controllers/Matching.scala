package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.MatchingService
import scala.concurrent.ExecutionContext.Implicits.global

class Matching(matchingService: MatchingService) extends Controller {

  def matchDemands() = Action.async {
    matchingService.matchDemands().map {
      demands => Ok(Json.obj("demands" -> Json.toJson(demands)))
    }
  }

}
