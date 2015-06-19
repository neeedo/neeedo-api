package migrations

import common.domain._
import common.helper.ConfigLoader
import common.logger.MigrationsLogger
import common.sphere.SphereClient
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.PagedQueryResult
import io.sphere.sdk.products.Product
import services.{UserService, OfferService, DemandService}
import scala.concurrent.Future
import common.helper.ImplicitConversions.OptionConverter
import scala.concurrent.ExecutionContext.Implicits.global

class ProductTestDataMigrations(sphereClient: SphereClient, demandService: DemandService,
                                offerService: OfferService, configloader: ConfigLoader,
                                userService: UserService) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Product Test Data Migrations started")
    if (configloader.getBoolean("sphere.IO.createTestData")) {
      val queryResult: Future[PagedQueryResult[Product]] = sphereClient.execute(ProductQuery.of())
      val option: Future[Unit] = queryResult.flatMap {
        res =>
          val result: Option[Product] = res.head().asScala

          result match {
          case Some(product) =>
            Future.successful(MigrationsLogger.info("-> You have already products in your sphere.IO plattform."))
          case None =>
            for {
              user <- createUser()
              demands <- createDemands(user)
              offers <- createOffers(user)
            } yield deleteUser(user)
        }
      }
      option
    } else {
      Future.successful[Unit](MigrationsLogger.info("-> Nothing to do"))
    }
  }

  def deleteUser(user: User) = userService.deleteUser(user.id, user.version)

  def createUser(): Future[User] = {
    userService.createUser(UserDraft(Username("MigrationsUser"), Email("migration@migration.de"), "migration"))
  }

  def createDemands(user: User): Future[Unit] = {
    val demandDraft1 = DemandDraft(user.id, Set("socken", "bekleidung", "wolle", "berlin"), Set("socken", "bekleidung", "wolle"), Location(Longitude(13.402), Latitude(52.52)), Distance(50), Price(20.0), Price(40.0))
    val demandDraft2 = DemandDraft(user.id, Set("fahrrad", "rot", "mountainbike", "berlin"), Set("socken", "bekleidung", "wolle"), Location(Longitude(13.402), Latitude(52.52)), Distance(100), Price(0.0), Price(100.0))
    val demandDraft3 = DemandDraft(user.id, Set("sofa", "stoff", "rot", "hamburg"), Set("socken", "bekleidung", "wolle"), Location(Longitude(9.99), Latitude(53.55)), Distance(30), Price(100.0), Price(340.0))
    val demandDraft4 = DemandDraft(user.id, Set("iphone", "münchen"), Set("neuwertig", "schwarz"), Location(Longitude(11.55), Latitude(48.14)), Distance(30), Price(100.0), Price(340.0))
    val demandDrafts = demandDraft1 :: demandDraft2 :: demandDraft3 :: demandDraft4 :: Nil

    Future.sequence { demandDrafts.map(demandService.createDemand) }.map { x => MigrationsLogger.info("-> Creating Test Demands")}
  }

  def createOffers(user: User): Future[Unit] = {
    val offerDraft1 = OfferDraft(user.id, Set("smartphone", "neuwertig", "iphone", "berlin"), Location(Longitude(13.402), Latitude(52.52)), Price(299.95), Set())
    val offerDraft2 = OfferDraft(user.id, Set("playstation3", "gebraucht", "berlin"), Location(Longitude(13.402), Latitude(52.52)), Price(299.95), Set())
    val offerDraft3 = OfferDraft(user.id, Set("bürostuhl", "armlehnen", "schwarz", "hamburg"), Location(Longitude(9.99), Latitude(53.55)), Price(299.95), Set())
    val offerDraft4 = OfferDraft(user.id, Set("iphone", "gebraucht", "münchen"), Location(Longitude(11.55), Latitude(48.14)), Price(299.95), Set())
    val offerDrafts = offerDraft1 :: offerDraft2 :: offerDraft3 :: offerDraft4 :: Nil

    Future.sequence { offerDrafts.map(offerService.createOffer) }.map { x => MigrationsLogger.info("-> Creating Test Offers")}
  }
}
