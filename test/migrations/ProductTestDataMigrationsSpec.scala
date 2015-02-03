package migrations

import java.util.Locale
import java.util.concurrent.TimeUnit

import common.domain._
import common.sphere.{ProductTypeDrafts, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.products.{ProductVariantBuilder, ProductCatalogDataBuilder, ProductBuilder, ProductDataBuilder, Product}
import io.sphere.sdk.queries.PagedQueryResult
import model.{OfferId, Offer, DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import services.{OfferService, DemandService}
import test.TestApplications
import scala.collection.JavaConverters._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration

class ProductTestDataMigrationsSpec extends Specification with Mockito {

  "ProductTestDataMigrations" should {
    "create Demands and Offers when sphere.IO.createTestdata is set to true and no products exist" in
      TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> true)) {
        val demand = Demand(DemandId("1"), Version(1L), UserId("1"), Set("socken", "bekleidung", "wolle"), Set("tags"), Location(Longitude(1.0), Latitude(1.0)), Distance(30), Price(40.0), Price(60.0))
        val offer = Offer(OfferId("1"), Version(1L), UserId("1"), "tags", Location(Longitude(1.0), Latitude(1.0)), Price(70.0))
        val sphereClient = mock[SphereClient]
        val demandService = mock[DemandService]
        val offerService = mock[OfferService]
        val emptyPagedResult = PagedQueryResult.empty[Product]()
        sphereClient.execute(ProductQuery.of()) returns Future.successful(emptyPagedResult)
        demandService.createDemand(any[DemandDraft]) returns Future.successful(Option(demand))
        offerService.createOffer(any[OfferDraft]) returns Future.successful(Option(offer))
        demandService.writeDemandToEs(demand) returns Future.successful(DemandSaved)
        offerService.writeOfferToEs(offer) returns Future.successful(OfferSaved)

        val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService)
        Await.result(productTestDataMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was one(sphereClient).execute(ProductQuery.of())
        there was three(demandService).createDemand(any[DemandDraft])
        there was three(offerService).createOffer(any[OfferDraft])
      }

    "not create Demands and Offers when sphere.IO.createTestdata is set to true and products exist" in
      TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> true)) {

        val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()
        val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle")
        val productVariant = ProductVariantBuilder.of(1).attributes(List.empty[Attribute].asJava).build()
        val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
        val product = ProductBuilder.of(productType, masterData).build()

        val sphereClient = mock[SphereClient]
        val demandService = mock[DemandService]
        val offerService = mock[OfferService]
        val nonEmptyPagedResult = PagedQueryResult.of(List(product).asJava)
        sphereClient.execute(ProductQuery.of()) returns Future.successful(nonEmptyPagedResult)

        val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService)
        Await.result(productTestDataMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was no(demandService).createDemand(any)
        there was no(offerService).createOffer(any)
      }

    "not create Demands and Offers when sphere.IO.createTestdata is set to false" in
      TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> false)) {

        val sphereClient = mock[SphereClient]
        val demandService = mock[DemandService]
        val offerService = mock[OfferService]
        val emptyPagedResult = PagedQueryResult.of(List.empty[Product].asJava)
        sphereClient.execute(ProductQuery.of()) returns Future.successful(emptyPagedResult)

        val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService)
        Await.result(productTestDataMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was no(demandService).createDemand(any)
        there was no(offerService).createOffer(any)
      }
  }
}
