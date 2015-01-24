package controllers

import common.domain._
import model.{Demand, DemandId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.DemandService
import scala.collection.immutable.Nil
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Demands(demandService: DemandService) extends Controller {
  val demandDraft1 = DemandDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

  val demand1 = Demand(DemandId("1"), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
  val demand2 = Demand(DemandId("2"), UserId("2"), "auto lack blau", Location(Longitude(52.468562), Latitude(13.534212)), Distance(40), Price(150.0), Price(300.0))
  val demand3 = Demand(DemandId("3"), UserId("3"), "notebook kein apple scheiss", Location(Longitude(20.0), Latitude(10.0)), Distance(25), Price(500.0), Price(1000.0))

  def listDemands = Action {
    val demands = demand1 :: demand2 :: demand3 :: Nil

    Ok(Json.obj("demands" -> Json.toJson(demands)))
  }

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

  def createDemandStub = Action {
    implicit request =>
      request.body.asJson match {
      case Some(json) =>
        json.asOpt[DemandDraft] match {
          case Some(demandDraft) => Created(Json.parse("""{"demand": { "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90", "userId": "1", "tags": "neues produkt pls 1312341234", "location": { "lat":13.534212, "lon":52.468562 }, "distance": 30, "price": { "min":25.0, "max":77.0 } } }"""))
          case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
        }
      case None => BadRequest(Json.obj("error" -> "Missing body"))
    }
  }

  //
  def fetchDemand(id: DemandId): Option[Demand] = if (id.value == "1") Some(demand1) else None

  def getDemandStub(id: DemandId) = Action {
    fetchDemand(id) match {
      case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def getDemand(id: DemandId) = Action.async {
    demandService.getDemandById(id).map {
      case Some(demand) => Ok(Json.toJson(demand))
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def updateDemand(id: DemandId) = Action {
    implicit request =>
      fetchDemand(id) match {
      case Some(demand) =>
        request.body.asJson match {
          case Some(json) =>
            json.asOpt[Demand] match {
              case Some(x) => Ok
              case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
            }
          case None => BadRequest(Json.obj("error" -> "Missing body"))
        }
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def deleteDemand(id: DemandId) = Action {
    fetchDemand(id) match {
      case Some(demand) => Ok
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def test = Action.async {
    demandService.writeDemandToSphere(demandDraft1).map(x => Ok(x.toString))
  }
}
