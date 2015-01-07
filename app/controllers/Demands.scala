package controllers

import common.domain._
import model.{Demand, DemandId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.DemandService

import scala.collection.immutable.Nil

class Demands(demandService: DemandService) extends Controller {
  def listDemands = Action {
    val demand1 = Demand(DemandId("1"), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
    val demand2 = Demand(DemandId("2"), UserId("2"), "auto lack blau", Location(Longitude(52.468562), Latitude(13.534212)), Distance(40), Price(150.0), Price(300.0))
    val demand3 = Demand(DemandId("3"), UserId("3"), "notebook kein apple scheiss", Location(Longitude(20.0), Latitude(10.0)), Distance(25), Price(500.0), Price(1000.0))

    val demands = demand1 :: demand2 :: demand3 :: Nil

    Ok(Json.obj("demands" -> Json.toJson(demands)))
  }

  def createDemand = Action {
    implicit request =>
      request.body.asJson match {
      case Some(js) =>
        js.asOpt[Demand] match {
          case Some(demand) => Ok
          case None => BadRequest("Cannot parse json")
        }
      case None => BadRequest("Missing body")
    }
  }

  def fetchDemand(id: DemandId) = id.value != "10"

  def getDemand(id: DemandId) = Action {
    if ( !fetchDemand(id) ) NotFound
    else {
      val demand = Demand(id, UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

      Ok(Json.obj("demand" -> Json.toJson(demand)))
    }
  }
  
  def updateDemand(id: DemandId) = TODO
  def deleteDemand(id: DemandId) = TODO
}
