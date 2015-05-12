package migrations

import java.util.Locale
import java.util.concurrent.TimeUnit

import common.domain._
import common.helper.ConfigLoader
import common.sphere.{ProductTypeDrafts, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.products.{ProductVariantBuilder, ProductCatalogDataBuilder, ProductBuilder, ProductDataBuilder, Product}
import io.sphere.sdk.queries.PagedQueryResult
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Play
import services.{OfferService, DemandService}
import test.{TestData, TestApplications}
import scala.collection.JavaConverters._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration

class ProductTestDataMigrationsSpec extends Specification with Mockito {

  "ProductTestDataMigrations" should {
    "create Demands and Offers when sphere.IO.createTestdata is set to true and no products exist" in
      TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> true)) {
        val configLoader = new ConfigLoader
        val demand = TestData.demand
        val offer = TestData.offer
        val sphereClient = mock[SphereClient]
        val demandService = mock[DemandService]
        val offerService = mock[OfferService]
        val emptyPagedResult = PagedQueryResult.empty[Product]()
        sphereClient.execute(ProductQuery.of()) returns Future.successful(emptyPagedResult)
        demandService.createDemand(any[DemandDraft]) returns Future.successful(Option(demand))
        offerService.createOffer(any[OfferDraft]) returns Future.successful(offer)
        demandService.writeDemandToEs(demand) returns Future.successful(DemandSaved)
        offerService.writeOfferToEs(offer) returns Future.successful(offer)

        val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService, configLoader)
        Await.result(productTestDataMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was one(sphereClient).execute(ProductQuery.of())
        there was 4.times(demandService).createDemand(any[DemandDraft])
        there was 4.times(offerService).createOffer(any[OfferDraft])
      }

    "not create Demands and Offers when sphere.IO.createTestdata is set to true and products exist" in
      TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> true)) {

        val configLoader = new ConfigLoader
        val productTypeDrafts = new ProductTypeDrafts(configLoader)
        val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
        val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle")
        val productVariant = ProductVariantBuilder.of(1).attributes(List.empty[Attribute].asJava).build()
        val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
        val product = ProductBuilder.of(productType, masterData).build()

        val sphereClient = mock[SphereClient]
        val demandService = mock[DemandService]
        val offerService = mock[OfferService]
        val nonEmptyPagedResult = PagedQueryResult.of(List(product).asJava)
        sphereClient.execute(ProductQuery.of()) returns Future.successful(nonEmptyPagedResult)

        val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService, configLoader)
        Await.result(productTestDataMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was no(demandService).createDemand(any)
        there was no(offerService).createOffer(any)
      }

    "not create Demands and Offers when sphere.IO.createTestdata is set to false" in
      TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> false)) {

        val configLoader = new ConfigLoader
        val sphereClient = mock[SphereClient]
        val demandService = mock[DemandService]
        val offerService = mock[OfferService]
        val emptyPagedResult = PagedQueryResult.of(List.empty[Product].asJava)
        sphereClient.execute(ProductQuery.of()) returns Future.successful(emptyPagedResult)

        val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService, configLoader)
        Await.result(productTestDataMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was no(demandService).createDemand(any)
        there was no(offerService).createOffer(any)
      }
  }
}
