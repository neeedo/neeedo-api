import common.elasticsearch.ElasticsearchClientFactory
import common.sphere.SphereClientFactory
import controllers._
import migrations.ProductTypeMigrations
import services.{MatchingService, OfferService, DemandService}

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  //Clients
  lazy val sphereClient   = SphereClientFactory()
  lazy val elasticsearchClient = ElasticsearchClientFactory()

  // Services
  lazy val demandService = wire[DemandService]
  lazy val offerService = wire[OfferService]
  lazy val matchingService = wire[MatchingService]

  // Controllers
  lazy val demandController = wire[Demands]
  lazy val documentationController = wire[Documentation]
  lazy val offerController = wire[Offers]
  lazy val demandsStubController = wire[DemandsStub]
  lazy val offersStubController = wire[OffersStub]

  // Migrations
  lazy val productTypeMigration = wire[ProductTypeMigrations]
}
