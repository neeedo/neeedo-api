package services

import common.domain._
import model.{Demand, DemandId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.es.EsDemandService
import services.sphere.SphereDemandService

import scala.concurrent.Future

class DemandService(sphereDemandService: SphereDemandService, esDemandService: EsDemandService) {

  def createDemand(draft: DemandDraft): Future[Demand] = {
    sphereDemandService.createDemand(draft).flatMap {
      demand => esDemandService.createDemand(demand).recoverWith {
        case e: Exception =>
          sphereDemandService.deleteDemand(demand.id, demand.version)
          throw e
      }
    }
  }

  def getDemandById(id: DemandId): Future[Option[Demand]] = {
    sphereDemandService.getDemandById(id)
  }

  def getDemandsByUserId(id: UserId): Future[List[Demand]] = {
    esDemandService.getDemandsByUserId(id)
  }

  def updateDemand(id: DemandId, version: Version, draft: DemandDraft): Future[Demand] = {
    createDemand(draft) flatMap {
      demand => deleteDemand(id, version)
    }
  }

  def deleteDemand(id: DemandId, version: Version): Future[Demand] = {
    esDemandService.deleteDemand(id).flatMap {
      demandId => sphereDemandService.deleteDemand(demandId, version)
    } recover {
      case e: Exception => throw e
    }
  }

  def deleteAllDemands(): Future[Any] = {
    esDemandService.deleteAllDemands()
    sphereDemandService.deleteAllDemands()
  }
}
