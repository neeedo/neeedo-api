package controllers

import common.domain._
import common.helper.SecuredAction
import model.DemandId
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.DemandService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Demands(service: DemandService) extends Controller {

  def createDemand = SecuredAction.async {
    implicit request => request.body.asJson match {
      case Some(json) => json.asOpt[DemandDraft] match {
          case Some(draft) => service.createDemand(draft).map {
            case Some(demand) => Created(Json.obj("demand" -> Json.toJson(demand)))
            case _ => BadRequest(Json.obj("error" -> "Unknown error"))
          }
          case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
        }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def getDemand(id: DemandId) = Action.async {
    service.getDemandById(id).map {
      case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def updateDemand(id: DemandId, version: Version) = SecuredAction.async {
    implicit request => request.body.asJson match {
      case Some(json) => json.asOpt[DemandDraft] match {
          case Some(draft) => service.updateDemand(id, version, draft).map {
            case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
            case _ => BadRequest(Json.obj("error" -> "Unknown error"))
          }
          case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
        }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def deleteDemand(id: DemandId, version: Version) = SecuredAction.async {
    service.deleteDemand(id, version).map {
      case Some(product) => Ok
      case None => NotFound
    }
  }
}
