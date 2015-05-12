package migrations

import common.domain._
import common.helper.ConfigLoader
import common.logger.MigrationsLogger
import common.sphere.SphereClient
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.PagedQueryResult
import io.sphere.sdk.products.Product
import services.{DemandService, OfferService}
import scala.concurrent.Future
import common.helper.ImplicitConversions.OptionConverter
import scala.concurrent.ExecutionContext.Implicits.global

class ProductTestDataMigrations(sphereClient: SphereClient, demandService: DemandService,
                                offerService: OfferService, configloader: ConfigLoader) extends Migration {

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
              demands <- createDemands()
              offers <- createOffers()
            } yield {
              (demands, offers)
            }
        }
      }
      option
    } else {
      Future.successful[Unit](MigrationsLogger.info("-> Nothing to do"))
    }
  }

  def createDemands(): Future[Unit] = {
    val demandDraft1 = DemandDraft(UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(50), Price(20.0), Price(40.0))
    val demandDraft2 = DemandDraft(UserId("2"), Set("fahrrad", "rot", "mountainbike"), Set("socken", "bekleidung", "wolle"), Location(Longitude(34.887512), Latitude(8.7374)), Distance(100), Price(0.0), Price(100.0))
    val demandDraft3 = DemandDraft(UserId("1"), Set("sofa", "stoff", "rot"), Set("socken", "bekleidung", "wolle"), Location(Longitude(12.37528), Latitude(35.92516)), Distance(30), Price(100.0), Price(340.0))
    val demandDraft4 = DemandDraft(UserId("1"), Set("iphone"), Set("neuwertig", "schwarz"), Location(Longitude(12.37528), Latitude(35.92516)), Distance(30), Price(100.0), Price(340.0))
    val demandDrafts = demandDraft1 :: demandDraft2 :: demandDraft3 :: demandDraft4 :: Nil

    Future.sequence { demandDrafts.map(demandService.createDemand) }.map { x => MigrationsLogger.info("-> Creating Test Demands")}
  }

  def createOffers(): Future[Unit] = {
    val offerDraft1 = OfferDraft(UserId("1"), Set("smartphone", "neuwertig", "iphone"), Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDraft2 = OfferDraft(UserId("1"), Set("playstation3", "gebraucht"), Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDraft3 = OfferDraft(UserId("1"), Set("bürostuhl", "armlehnen", "schwarz"), Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDraft4 = OfferDraft(UserId("1"), Set("iphone", "gebraucht"), Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDrafts = offerDraft1 :: offerDraft2 :: offerDraft3 :: offerDraft4 :: Nil

    Future.sequence { offerDrafts.map(offerService.createOffer) }.map { x => MigrationsLogger.info("-> Creating Test Offers")}
  }
}
