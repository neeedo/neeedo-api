package common.helper
import common.amazon.S3ClientFactory
import common.elasticsearch.{ElasticsearchClient, ElasticsearchClientFactory}
import common.sphere._
import controllers._
import migrations._
import play.api.{Mode, Play}
import services._
import services.es.EsOfferService
import services.sphere.SphereOfferService

trait WireDependencies {
  import com.softwaremill.macwire.MacwireMacros._

  lazy val configLoader = new ConfigLoader(Play.current.configuration)
  lazy val productTypeDrafts = wire[ProductTypeDrafts]
  lazy val productTypes: ProductTypes = if (Play.current.mode == Mode.Test) MockProductTypes else wire[SphereProductTypes]

  // Factories
  val esFactory: ElasticsearchClientFactory = wire[ElasticsearchClientFactory]

  //Clients
  val s3Client = wire[S3ClientFactory].instance
  val sphereClient = wire[SphereClientFactory].instance
  val elasticsearchClient: ElasticsearchClient = esFactory.instance

  // Services
  lazy val demandService = wire[DemandService]
  lazy val offerService = wire[OfferService]
  lazy val matchingService = wire[MatchingService]
  lazy val completionService = wire[CompletionService]
  lazy val userService = wire[UserService]
  lazy val uploadService = wire[UploadService]
  //// Elasticsearch
  lazy val esOfferService = wire[EsOfferService]
  lazy val esMatchingService = wire[EsMatchingService]
  lazy val esCompletionService = wire[EsCompletionService]
  //// Sphere
  lazy val sphereOfferService = wire[SphereOfferService]

  // Controllers
  lazy val demandController = wire[DemandsController]
  lazy val offerController = wire[OffersController]
  lazy val matchingController = wire[MatchingController]
  lazy val completionController = wire[CompletionController]
  lazy val staticController = wire[StaticController]
  lazy val userController = wire[UsersController]
  lazy val imagesController = wire[ImagesController]

  // Migrations
  lazy val productTypeMigration = wire[ProductTypeMigrations]
  lazy val productTypeEsMigration = wire[ProductTypeEsMigrations]
  lazy val completionsEsMigration = wire[CompletionsEsMigrations]
  lazy val productTestDataMigration = wire[ProductTestDataMigrations]
  lazy val amazons3Migration = wire[AmazonS3Migrations]
}
