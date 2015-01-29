package migrations

import java.util.Locale

import common.domain.{OfferDraft, DemandDraft}
import common.sphere.{ProductTypeDrafts, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.products._
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.queries.PagedQueryResult
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import services.{OfferService, DemandService}
import test.TestApplications
import scala.collection.JavaConverters._

import scala.concurrent.Future

class ProductTestDataMigrationsSpec extends Specification with Mockito {

  "ProductTestDataMigrations" should {
        "create Demands and Offers when sphere.IO.createTestdata is set to true and no products exist" in
          TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> true)){

          val sphereClient = mock[SphereClient]
          val demandService = mock[DemandService]
          val offerService = mock[OfferService]
          val emptyPagedResult = PagedQueryResult.of(List.empty[Product].asJava)
          sphereClient.execute(ProductQuery.of()) returns Future.successful(emptyPagedResult)

          val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService)
          productTestDataMigrations.run()

          there was one (sphereClient).execute(ProductQuery.of())
          there was three (demandService).createDemand(any[DemandDraft])
          there was three (offerService).createOffer(any[OfferDraft])
        }
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
          productTestDataMigrations.run()

          there was no (demandService).createDemand(any)
          there was no (offerService).createOffer(any)
        }

      "not create Demands and Offers when sphere.IO.createTestdata is set to false" in
        TestApplications.loggingOffApp(Map("sphere.IO.createTestData" -> false)) {

          val sphereClient = mock[SphereClient]
          val demandService = mock[DemandService]
          val offerService = mock[OfferService]
          val emptyPagedResult = PagedQueryResult.of(List.empty[Product].asJava)
          sphereClient.execute(ProductQuery.of()) returns Future.successful(emptyPagedResult)

          val productTestDataMigrations = new ProductTestDataMigrations(sphereClient, demandService, offerService)
          productTestDataMigrations.run()

          there was no (demandService).createDemand(any)
          there was no (offerService).createOffer(any)
  }
}
