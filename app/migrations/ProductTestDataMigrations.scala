package migrations

import common.domain._
import common.helper.Configloader
import common.sphere.SphereClient
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.PagedQueryResult
import io.sphere.sdk.products.Product
import play.api.Logger
import services.{DemandService, OfferService}
import scala.concurrent.Future
import common.helper.ImplicitConversions.optionalToOption
import scala.concurrent.ExecutionContext.Implicits.global

class ProductTestDataMigrations(sphereClient: SphereClient, demandService: DemandService, offerService: OfferService) extends Migration {

  override def run(): Unit = {
    if (Configloader.getBoolean("sphere.IO.createTestData")) {
      val queryResult: Future[PagedQueryResult[Product]] = sphereClient.execute(ProductQuery.of())
      val option: Future[Option[Product]] = queryResult.map(res => res.head())

      option.map {
        case Some(product) =>
          Logger.info("You have products in your sphere.IO plattform. Test data won't be imported. You can deactivate this message by setting sphere.IO.createTestdata to false in your custom-application.conf")
        case None =>
          createDemands()
          Logger.info("Creating Test Demands")
          createOffers()
          Logger.info("Creating Test Offers")
      }
    }
  }

  def createDemands(): Unit = {
    val demandDraft1 = DemandDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(50), Price(20.0), Price(40.0))
    val demandDraft2 = DemandDraft(UserId("2"), "fahrrad rot mountainbike", Location(Longitude(34.887512), Latitude(8.7374)), Distance(100), Price(0.0), Price(100.0))
    val demandDraft3 = DemandDraft(UserId("1"), "sofa stoff rot", Location(Longitude(12.37528), Latitude(35.92516)), Distance(30), Price(100.0), Price(340.0))
    val demandDrafts = demandDraft1 :: demandDraft2 :: demandDraft3 :: Nil

    demandDrafts.map(demandService.createDemand)
  }

  def createOffers(): Unit = {
    val offerDraft1 = OfferDraft(UserId("1"), "smartphone neuwertig iphone", Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDraft2 = OfferDraft(UserId("1"), "playstation3 gebraucht", Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDraft3 = OfferDraft(UserId("1"), "stuhl büro armlehnen", Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))
    val offerDrafts = offerDraft1 :: offerDraft2 :: offerDraft3 :: Nil

    offerDrafts.map(offerService.createOffer)
  }
}
