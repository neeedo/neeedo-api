package controllers

import model.DemandId
import play.api.mvc.Controller
import services.DemandService

class Demands(demandService: DemandService) extends Controller {
  def listDemands = TODO
  def getDemand(id: DemandId) = TODO
  def createDemand = TODO
  def updateDemand(id: DemandId) = TODO
  def deleteDemand(id: DemandId) = TODO
}
