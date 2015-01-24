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
            case Some(demandDraft) => demandService.addDemand(demandDraft).map {
              case Some(demand) => Created(Json.obj("demand" -> Json.toJson(demand)))
              case _ => BadRequest(Json.obj("error" -> "Unknown error"))
            }
            case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
          }
        case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
      }
  }

  // Todo
  def listDemands = Action {
    Ok
  }

  def getDemand(id: DemandId) = Action.async {
    demandService.getDemandById(id).map {
      case Some(demand) => Ok(Json.toJson(demand))
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  // See DemandService::updateDemand
  def updateDemand(id: DemandId) =Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.asOpt[DemandDraft] match {
          case Some(demandDraft) => demandService.updateDemand(id, demandDraft).map {
            case Some(demand) => Accepted(Json.obj("demand" -> Json.toJson(demand)))
            case _ => NotFound(Json.obj("error" -> "Entity was not found"))
          }
          case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
        }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def deleteDemand(id: DemandId) = Action.async {
    demandService.deleteDemand(id).map {
      case Some(product) => Ok
      case None => NotFound
    }
  }

  // obsolete?
  def test = Action {Ok} // demandService.writeDemandToSphere(demandDraft1).map(Ok(_.toString))

}
