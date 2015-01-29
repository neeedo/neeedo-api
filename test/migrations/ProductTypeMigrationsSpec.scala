package migrations

import common.sphere.{ProductTypeDrafts, SphereClient}
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import io.sphere.sdk.queries.PagedQueryResult
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.TestApplications
import scala.collection.JavaConverters._


import scala.concurrent.Future

class ProductTypeMigrationsSpec extends Specification with Mockito {
  "ProductTypeMigrations" should {

    "create offer and demand producttypes when they don't exist in Sphere.IO" in TestApplications.loggingOffApp() {
      val sphereClient = mock[SphereClient]
      val emptyPagedResult = PagedQueryResult.of(List.empty[ProductType].asJava)
      sphereClient.execute(ProductTypeQuery.of().byName(ProductTypeDrafts.demand.getName)) returns Future.successful(emptyPagedResult)
      sphereClient.execute(ProductTypeQuery.of().byName(ProductTypeDrafts.offer.getName)) returns Future.successful(emptyPagedResult)

      val productTypeMigrations: ProductTypeMigrations = new ProductTypeMigrations(sphereClient)
      productTypeMigrations.run()

      there was one (sphereClient).execute(ProductTypeCreateCommand.of(ProductTypeDrafts.demand))
      there was one (sphereClient).execute(ProductTypeCreateCommand.of(ProductTypeDrafts.offer))
    }

    "do nothing when offer and demand producttypes exist in Sphere.IO" in TestApplications.loggingOffApp() {
      val sphereClient = mock[SphereClient]
      val productType = ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      val nonEmptyPagedResult = PagedQueryResult.of(List(productType).asJava)
      sphereClient.execute(ProductTypeQuery.of().byName(ProductTypeDrafts.demand.getName)) returns Future.successful(nonEmptyPagedResult)
      sphereClient.execute(ProductTypeQuery.of().byName(ProductTypeDrafts.offer.getName)) returns Future.successful(nonEmptyPagedResult)

      val productTypeMigrations: ProductTypeMigrations = new ProductTypeMigrations(sphereClient)
      productTypeMigrations.run()

      there was no (sphereClient).execute(ProductTypeCreateCommand.of(ProductTypeDrafts.demand))
      there was no (sphereClient).execute(ProductTypeCreateCommand.of(ProductTypeDrafts.offer))
    }
  }
}
