package services

import common.domain._
import common.sphere.{ProductTypes, SphereClient}
import model.{Demand, Offer}
import services.es.EsMatchingService

import scala.concurrent.Future

class MatchingService(sphereClient: SphereClient, esMatching: EsMatchingService,
                      productTypes: ProductTypes) {

  def matchDemand(pager: Pager, demand: Demand): Future[List[Offer]] = {
    esMatching.matchDemand(pager, demand)
  }
}

