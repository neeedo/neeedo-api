package controllers

import common.domain._
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.{Demand, DemandId}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.DemandService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

class DemandsController(service: DemandService) extends Controller with ControllerUtils {

  def createDemand = SecuredAction.async { implicit request =>
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

  def getDemandById(id: DemandId) = SecuredAction.async {
    service.getDemandById(id).map {
      case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
      case None => NotFound(Json.obj("error" -> "Demand not found"))
    }
  }

  def getDemandsByUserId(id: UserId) = SecuredAction.async {
    service.getDemandsByUserId(id).map { demands: List[Demand] =>
      Ok(Json.obj("demands" -> Json.toJson(demands)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def updateDemand(id: DemandId, version: Version) = SecuredAction.async { implicit request =>
    val demandDraft = bindRequestJsonBody(request)(DemandDraft.demandDraftReads)

    demandDraft match {
      case Success(o) =>
        service.updateDemand(id, version, o) map {
          demand => Ok(Json.obj("demand" -> Json.toJson(demand)))
        } recover { case e: Exception => e.asResult }
      case Failure(e) => Future(e.asResult)
    }
  }


  def deleteDemand(id: DemandId, version: Version) = SecuredAction.async {
    service.deleteDemand(id, version)
      .map(_ => Ok)
      .recover {
      case e: Exception => e.asResult
    }
  }

  def deleteAllDemands() = SecuredAction.async {
    service.deleteAllDemands()
      .map(_ => Ok)
      .recover { case e: Exception => e.asResult }
  }
}