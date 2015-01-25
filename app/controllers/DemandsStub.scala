package controllers

import common.domain._
import model.{Demand, DemandId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.collection.immutable.Nil

class DemandsStub extends Controller {
  val demandDraft1 = DemandDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

  val demand1 = Demand(DemandId("1"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
  val demand2 = Demand(DemandId("2"), Version(1L), UserId("2"), "auto lack blau", Location(Longitude(52.468562), Latitude(13.534212)), Distance(40), Price(150.0), Price(300.0))
  val demand3 = Demand(DemandId("3"), Version(1L), UserId("3"), "notebook kein apple scheiss", Location(Longitude(20.0), Latitude(10.0)), Distance(25), Price(500.0), Price(1000.0))

  def fetchDemand(id: DemandId, version: Version): Option[Demand] = (id.value, version.value) match {
    case ("1", 1L) => Some(demand1)
    case ("2", 1L) => Some(demand1)
    case ("3", 1L) => Some(demand1)
    case (_, _)    => None
  }

  def fetchDemand(id: DemandId) : Option[Demand] = id.value match {
    case "1" => Some (demand1)
    case "2" => Some (demand2)
    case "3" => Some (demand3)
    case _   => None
  }

  def createDemand = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.asOpt[DemandDraft] match {
          case Some(demandDraft) => Created(Json.obj("demand" -> Json.toJson(demand1)))
          case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
        }
      case None => BadRequest(Json.obj("error" -> "Missing body"))
    }
  }

  // TODO list könnte später vom matching controller übernommen werden?
  def listDemands = Action {
    val demands = demand1 :: demand2 :: demand3 :: Nil
    Ok(Json.obj("demands" -> Json.toJson(demands)))
  }

  def getDemand(id: DemandId) = Action {
    fetchDemand(id) match {
      case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def updateDemand(id: DemandId, version: Version) = Action { implicit request =>
    fetchDemand(id, version) match {
      case Some(demand) =>
        request.body.asJson match {
          case Some(json) =>
            json.asOpt[DemandDraft] match {
              case Some(x) => Ok
              case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
            }
          case None => BadRequest(Json.obj("error" -> "Missing body"))
        }
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }

  def deleteDemand(id: DemandId, version: Version) = Action {
    fetchDemand(id, version) match {
      case Some(demand) => Ok
      case None => NotFound(Json.obj("error" -> "Demand Entity not found"))
    }
  }
}