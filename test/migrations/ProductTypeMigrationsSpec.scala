//package migrations
//
//import java.util.concurrent.TimeUnit
//
//import common.helper.ConfigLoader
//import common.sphere.{ProductTypeDrafts, SphereClient}
//import io.sphere.sdk.attributes.AttributeDefinition
//import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand
//import io.sphere.sdk.producttypes.queries.ProductTypeQuery
//import io.sphere.sdk.producttypes.{ProductType, ProductTypeBuilder}
//import io.sphere.sdk.queries.PagedQueryResult
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//import test.TestApplications
//
//import scala.collection.JavaConverters._
//import scala.concurrent.duration.FiniteDuration
//import scala.concurrent.{Await, Future}
//
//class ProductTypeMigrationsSpec extends Specification with Mockito {
//  "ProductTypeMigrations" should {
//
//    "create offer and demand product types when they don't exist in Sphere.IO" in TestApplications.loggingOffApp() {
//      val configLoader = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(configLoader)
//      val sphereClient = mock[SphereClient]
//      val emptyPagedResult = PagedQueryResult.empty[ProductType]()
//      sphereClient.execute(any[ProductTypeQuery]) returns Future.successful(emptyPagedResult)
//      sphereClient.execute(ProductTypeCreateCommand.of(productTypeDrafts.demand)) returns Future.successful(ProductTypeBuilder.of("","","", List.empty[AttributeDefinition].asJava).build())
//      sphereClient.execute(ProductTypeCreateCommand.of(productTypeDrafts.offer)) returns Future.successful(ProductTypeBuilder.of("","","", List.empty[AttributeDefinition].asJava).build())
//
//      val productTypeMigrations: ProductTypeMigrations = new ProductTypeMigrations(sphereClient, productTypeDrafts)
//      Await.result(productTypeMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))
//
//      there was one (sphereClient).execute(ProductTypeQuery.of().byName(productTypeDrafts.demand.getName))
//      there was one (sphereClient).execute(ProductTypeQuery.of().byName(productTypeDrafts.offer.getName))
//      there was one (sphereClient).execute(ProductTypeCreateCommand.of(productTypeDrafts.demand))
//      there was one (sphereClient).execute(ProductTypeCreateCommand.of(productTypeDrafts.offer))
//    }
//
//    "do nothing when offer and demand producttypes exist in Sphere.IO" in TestApplications.loggingOffApp() {
//      val configLoader = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(configLoader)
//      val sphereClient = mock[SphereClient]
//      val productType = ProductTypeBuilder.of("demand", productTypeDrafts.demand).build()
//      val nonEmptyPagedResult = PagedQueryResult.of(List(productType).asJava)
//      sphereClient.execute(ProductTypeQuery.of().byName(productTypeDrafts.demand.getName)) returns Future.successful(nonEmptyPagedResult)
//      sphereClient.execute(ProductTypeQuery.of().byName(productTypeDrafts.offer.getName)) returns Future.successful(nonEmptyPagedResult)
//
//      val productTypeMigrations: ProductTypeMigrations = new ProductTypeMigrations(sphereClient, productTypeDrafts)
//      Await.result(productTypeMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))
//
//      there was no (sphereClient).execute(ProductTypeCreateCommand.of(productTypeDrafts.demand))
//      there was no (sphereClient).execute(ProductTypeCreateCommand.of(productTypeDrafts.offer))
//    }
//  }
//}
