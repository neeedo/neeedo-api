import common.elasticsearch.ElasticsearchClientFactory
import common.sphere.SphereClientFactory
import controllers.Demand
import services.DemandService

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  //Clients
  lazy val sphereClient   = SphereClientFactory()
  lazy val elasticsearchClient = ElasticsearchClientFactory()

  // Services
  lazy val demandService = wire[DemandService]
  //lazy val offerService = wire[OfferService]

  // Controllers
  lazy val demandController = wire[Demand]
  //lazy val offerController = wire[OfferController]
}
