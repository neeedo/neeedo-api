package controllers

import common.domain._
import model.{Demand, DemandId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.DemandService

import scala.collection.immutable.Nil

class Demands(demandService: DemandService) extends Controller {
  val demand1 = Demand(DemandId("1"), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
  val demand2 = Demand(DemandId("2"), UserId("2"), "auto lack blau", Location(Longitude(52.468562), Latitude(13.534212)), Distance(40), Price(150.0), Price(300.0))
  val demand3 = Demand(DemandId("3"), UserId("3"), "notebook kein apple scheiss", Location(Longitude(20.0), Latitude(10.0)), Distance(25), Price(500.0), Price(1000.0))

  def listDemands = Action {
    val demands = demand1 :: demand2 :: demand3 :: Nil

    Ok(Json.obj("demands" -> Json.toJson(demands)))
  }

  def createDemand = Action {
    implicit request =>
      request.body.asJson match {
      case Some(js) =>
        js.asOpt[Demand] match {
          case Some(demand) => Ok
          case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
        }
      case None => BadRequest(Json.obj("error" -> "Missing body"))
    }
  }

  def fetchDemand(id: DemandId): Option[Demand] = if (id.value == "1") Some(demand1) else None

  def getDemand(id: DemandId) = Action {
    fetchDemand(id) match {
      case Some(demand) => Ok(Json.obj("demand" -> Json.toJson(demand)))
      case None => NotFound
    }
  }

  def updateDemand(id: DemandId) = Action {
    implicit request =>
      fetchDemand(id) match {
      case Some(demand) =>
        request.body.asJson match {
          case Some(js) =>
            js.asOpt[Demand] match {
              case Some(x) => Ok
              case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
            }
          case None => BadRequest(Json.obj("error" -> "Missing body"))
        }
      case None => NotFound
    }
  }

  def deleteDemand(id: DemandId) = Action {
    fetchDemand(id) match {
      case Some(demand) => Ok
      case None => NotFound
    }
  }
}