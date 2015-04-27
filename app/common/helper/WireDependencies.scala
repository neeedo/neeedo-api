package common.helper

import common.elasticsearch.ElasticsearchClientFactory
import common.sphere.{MockProductTypes, ProductTypes, SphereClientFactory, SphereProductTypes}
import controllers._
import migrations.{ProductTestDataMigrations, ProductTypeEsMigrations, ProductTypeMigrations}
import play.api.{Mode, Play}
import services._

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  //Clients
  val sphereClient = SphereClientFactory()
  val elasticsearchClient = ElasticsearchClientFactory()

  // Services
  lazy val demandService = wire[DemandService]
  lazy val offerService = wire[OfferService]
  lazy val esMatchingService = wire[EsMatchingService]
  lazy val matchingService = wire[MatchingService]
  lazy val userService = wire[UserService]

  // Controllers
  lazy val demandController = wire[Demands]
  lazy val offerController = wire[Offers]
  //lazy val demandsStubController = wire[DemandsStub]
  //lazy val offersStubController = wire[OffersStub]
  lazy val documentationController = wire[Documentation]
  lazy val matchingController = wire[Matching]
  lazy val staticController = wire[Static]
  lazy val userController = wire[Users]

  // Migrations
  lazy val productTypeMigration = wire[ProductTypeMigrations]
  lazy val productTypeEsMigration = wire[ProductTypeEsMigrations]
  lazy val productTestDataMigration = wire[ProductTestDataMigrations]

  // Common
  lazy val productTypes: ProductTypes = if (Play.current.mode == Mode.Test) MockProductTypes
  else wire[SphereProductTypes]
}