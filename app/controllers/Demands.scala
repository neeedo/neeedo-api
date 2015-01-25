package controllers

import common.domain._
import model.DemandId
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.DemandService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Demands(demandService: DemandService) extends Controller {

  def createDemand = Action.async {
    implicit request =>
      request.body.asJson match {
        case Some(json) =>
          json.asOpt[DemandDraft] match {
            case Some(demandDraft) => demandService.createDemand(demandDraft).map {
              case Some(demand) => Created(Json.obj("demand" -> Json.toJson(demand)))
              case _ => BadRequest(Json.obj("error" -> "Unknown error"))
            }
            case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
          }
        case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
      }
  }

  def getDemand(id: DemandId) = Action.async {
    demandService.getDemandById(id).map {
      case Some(demand) => Ok(Json.toJson(demand))
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def updateDemand(demandId: DemandId, version: Version) = Action.async {
    implicit request => request.body.asJson match {
      case Some(json) => json.asOpt[DemandDraft] match {
          case Some(demandDraft) => demandService.updateDemand(demandId, version, demandDraft).map {
            case Some(demand) => Created(Json.obj("demand" -> Json.toJson(demand)))
            case _ => BadRequest(Json.obj("error" -> "Unknown error"))
          }
          case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
        }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def deleteDemand(demandId: DemandId, version: Version) = Action.async {
    demandService.deleteDemand(demandId, version).map {
      case Some(product) => Ok
      case None => NotFound
    }
  }
}
