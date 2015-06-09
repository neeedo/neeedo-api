package controllers

import common.domain._
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.{Demand, DemandId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.DemandService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

class DemandsController(service: DemandService, securedAction: SecuredAction) extends Controller with ControllerUtils {

  val pagerOffsetDefault = 0
  val pagerLimitDefault = 20

  def createDemand = securedAction.async { implicit request =>
    val demandDraft = bindRequestJsonBody(request)(DemandDraft.demandDraftReads)

    demandDraft match {
      case Success(draft) => service.createDemand(draft) map {
        demand => Created(Json.obj("demand" -> Json.toJson(demand)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }

  def getDemandById(id: DemandId) = securedAction.async {
    service.getDemandById(id) map {
      case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
      case None => NotFound(Json.obj("error" -> "Demand not found"))
    }
  }

  def getDemandsByUserId(id: UserId, pagerOption: Option[Pager]) = securedAction.async {
    val pager = pagerOption.getOrElse(Pager(pagerLimitDefault, pagerOffsetDefault))
    service.getDemandsByUserId(id, pager) map { demands: List[Demand] =>
      Ok(Json.obj("demands" -> Json.toJson(demands)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def getAllDemands(pagerOption: Option[Pager]) = Action.async {
    val pager = pagerOption.getOrElse(Pager(pagerLimitDefault, pagerOffsetDefault))
    service.getAllDemands(pager) map { demands: List[Demand] =>
      Ok(Json.obj("demands" -> Json.toJson(demands)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def updateDemand(id: DemandId, version: Version) = securedAction.async { implicit request =>
    val demandDraft = bindRequestJsonBody(request)(DemandDraft.demandDraftReads)

    demandDraft match {
      case Success(o) =>
        service.updateDemand(id, version, o) map {
          demand => Ok(Json.obj("demand" -> Json.toJson(demand)))
        } recover { case e: Exception => e.asResult }
      case Failure(e) => Future(e.asResult)
    }
  }


  def deleteDemand(id: DemandId, version: Version) = securedAction.async {
    service.deleteDemand(id, version)
      .map(_ => Ok)
      .recover {
      case e: Exception => e.asResult
    }
  }

  def deleteAllDemands() = securedAction.async {
    service.deleteAllDemands()
      .map(_ => Ok)
      .recover { case e: Exception => e.asResult }
  }
}