package migrations

import common.domain._
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.OptionConverter
import common.logger.MigrationsLogger
import common.sphere.{ProductTypes, SphereClient, SphereClientFactory}
import io.sphere.sdk.products.Product
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import io.sphere.sdk.queries.PagedQueryResult
import model.Offer
import play.api.Configuration
import services.{DemandService, OfferService, UserService}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class ProductTestDataMigrations(sphereClient: SphereClient, demandService: DemandService,
                                offerService: OfferService, configloader: ConfigLoader,
                                userService: UserService) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Product Test Data Migrations started")
    if (configloader.getBoolean("sphere.IO.createTestData")) {
      val testDataSphereClient = buildTestDataClient

      sphereClient.execute(ProductQuery.of()).flatMap {
        res =>
          res.head().asScala match {
            case Some(product) =>
              Future.successful(MigrationsLogger.info("-> You have already products in your sphere.IO plattform."))
            case None =>
              for {
                user <- createUser()
                testProducts <- queryTestProducts(testDataSphereClient)
                testOffers <- convertProductsToOffers(testProducts)
                importedOffers <- importOffers(testOffers, user)
              } yield Unit
          }
      }
    } else {
      Future.successful[Unit](MigrationsLogger.info("-> Nothing to do"))
    }
  }

  def queryTestProducts(sphereClient: SphereClient) = {
    sphereClient
      .execute(ProductTypeQuery.of().byName(configloader.getString("offer.typeName")))
      .flatMap {
        prodType =>
          sphereClient
            .execute(ProductQuery.of().byProductType(prodType.getResults.get(0)).withLimit(500))
    }
  }

  def convertProductsToOffers(queryResult: PagedQueryResult[Product]): Future[List[Offer]] = {
    val products: List[Product] = queryResult.getResults.asScala.toList
    Future(products.map(Offer.fromProduct).collect {
      case offer: Success[Offer] => offer.get
    })
  }

  def importOffers(offers: List[Offer], user: User): Future[List[Offer]] = {
    Future.sequence(offers.map {
      offer =>
        offerService.createOffer(
          OfferDraft(user.id, offer.tags, offer.location, offer.price, offer.images))
    })
  }

  def createUser(): Future[User] = {
    userService.createUser(UserDraft(Username("MigrationsUser"), Email("migration@migration.de"), "migration"))
  }

  def buildTestDataClient = {
    val config = Map(
      "sphere.project" -> configloader.getString("sphere.testProject"),
      "sphere.clientId" -> configloader.getString("sphere.testClientId"),
      "sphere.clientSecret" -> configloader.getString("sphere.testClientSecret"))
    val testDataConfigLoader = new ConfigLoader(Configuration.from(config))
    new SphereClientFactory(testDataConfigLoader).getInstance
  }
}
