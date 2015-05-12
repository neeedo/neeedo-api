package common.helper

import common.elasticsearch.ElasticsearchClientFactory
import common.sphere._
import controllers._
import migrations.{CompletionsEsMigrations, ProductTestDataMigrations, ProductTypeEsMigrations, ProductTypeMigrations}
import play.api.{Mode, Play}
import services._

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  //Configloader
  val configLoader = wire[ConfigLoader]
  lazy val productTypeDrafts = wire[ProductTypeDrafts]
  lazy val productTypes: ProductTypes = if (Play.current.mode == Mode.Test) MockProductTypes else wire[SphereProductTypes]

  //Clients
  val sphereClient = wire[SphereClientFactory].instance
  val esFactory = wire[ElasticsearchClientFactory]
  val elasticsearchClient = wire[ElasticsearchClientFactory].instance

  // Services
  lazy val demandService = wire[DemandService]
  lazy val offerService = wire[OfferService]
  lazy val esMatchingService = wire[EsMatchingService]
  lazy val matchingService = wire[MatchingService]
  lazy val esCompletionService = wire[EsCompletionService]
  lazy val completionService = wire[CompletionService]
  lazy val userService = wire[UserService]

  // Controllers
  lazy val demandController = wire[Demands]
  lazy val offerController = wire[Offers]
  lazy val documentationController = wire[Documentation]
  lazy val matchingController = wire[Matching]
  lazy val completionController = wire[Completion]
  lazy val staticController = wire[Static]
  lazy val userController = wire[Users]

  // Migrations
  lazy val productTypeMigration = wire[ProductTypeMigrations]
  lazy val productTypeEsMigration = wire[ProductTypeEsMigrations]
  lazy val completionsEsMigration = wire[CompletionsEsMigrations]
  lazy val productTestDataMigration = wire[ProductTestDataMigrations]
}
