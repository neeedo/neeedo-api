import common.elasticsearch.ElasticsearchClientFactory
import common.sphere.SphereClientFactory

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  //Clients
  lazy val sphereClient   = SphereClientFactory()
  lazy val elasticsearchClient = ElasticsearchClientFactory()

  // Services
  //lazy val pagingService = wire[PagingService]
  //lazy val demandService = wire[DemandService]
  //lazy val offerService = wire[OfferService]

  // Controllers
  //lazy val pagingController = wire[PagingController]
  //lazy val demandController = wire[DemandController]
  //lazy val offerController = wire[OfferController]
}
