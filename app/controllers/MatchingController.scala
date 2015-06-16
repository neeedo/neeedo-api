package controllers

import common.domain.Pager
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.{Demand, Offer}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.MatchingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class MatchingController(matchingService: MatchingService, securedAction: SecuredAction) extends Controller with ControllerUtils {

  def matchDemand(p: Option[Pager]) = securedAction.async { implicit request =>
    val demand = bindRequestJsonBody(request)(Demand.demandReads)

    demand match {
      case Success(d) =>
        val pager = p.getOrElse(Pager(20, 0))
        matchingService.matchDemand(pager, d).map {
          offers: List[Offer] =>
            Ok(Json.obj("offers" -> Json.toJson(offers)))
        } recover {
          case e: Exception => e.asResult
        }
      case Failure(e) => Future(e.asResult)
    }
  }
}
