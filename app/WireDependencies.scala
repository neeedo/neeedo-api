import common.elasticsearch.ElasticsearchClientFactory
import common.sphere.{MockProductTypes, SphereProductTypes, ProductTypes, SphereClientFactory}
import controllers._
import migrations.{ProductTestDataMigrations, ProductTypeMigrations}
import play.api.Play
import services.{DocumentationService, MatchingService, OfferService, DemandService}
import play.api.Mode

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
  lazy val matchingController = wire[Matching]
  lazy val staticController = wire[Static]

  // Migrations
  lazy val productTypeMigration = wire[ProductTypeMigrations]
  lazy val productTestDataMigration = wire[ProductTestDataMigrations]

  // Common
  lazy val productTypes: ProductTypes = if (Play.current.mode == Mode.Test) wire[MockProductTypes]
  else wire[SphereProductTypes]
}
