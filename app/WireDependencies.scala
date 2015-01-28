import common.elasticsearch.ElasticsearchClientFactory
import common.sphere.{ProductTypes, SphereClientFactory}
import controllers._
import migrations.ProductTypeMigrations
import services.{DocumentationService, MatchingService, OfferService, DemandService}

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  //Clients
  lazy val sphereClient   = SphereClientFactory()
  lazy val elasticsearchClient = ElasticsearchClientFactory()

  // Services
  lazy val demandService = wire[DemandService]
  lazy val offerService = wire[OfferService]
  lazy val matchingService = wire[MatchingService]
  lazy val documentationService = wire[DocumentationService]

  // Controllers
  lazy val demandController = wire[Demands]
  lazy val offerController = wire[Offers]
  lazy val demandsStubController = wire[DemandsStub]
  lazy val offersStubController = wire[OffersStub]
  lazy val documentationController = wire[Documentation]

  // Migrations
  lazy val productTypeMigration = wire[ProductTypeMigrations]

  // Common
  lazy val productTypes = wire[ProductTypes]
}
