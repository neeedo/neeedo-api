package controllers

import model.DemandId
import play.api.mvc.Controller
import services.DemandService

class DemandController(demandService: DemandService) extends Controller {
  def listDemands = TODO
  def getDemand(id: DemandId) = TODO
  def createDemand(id: DemandId) = TODO
  def updateDemand(id: DemandId) = TODO
  def deleteDemand(id: DemandId) = TODO
}
