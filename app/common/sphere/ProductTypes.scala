package common.sphere

import common.helper.ConfigLoader
import io.sphere.sdk.attributes.AttributeDefinition
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

trait ProductTypes {
  def demand: ProductType
  def offer: ProductType
}

class SphereProductTypes(sphereClient: SphereClient, configloader: ConfigLoader) extends ProductTypes {
  lazy val demand: ProductType = queryDemandType
  lazy val offer: ProductType = queryOfferType

  private def queryDemandType: ProductType = {
    val typeName = configloader.getStringOpt("demand.typeName").get
    Await.result(sphereClient.execute(ProductTypeQuery.of().byName(typeName)), 10 seconds).getResults.get(0)
  }

  private def queryOfferType: ProductType = {
    val typeName = configloader.getStringOpt("offer.typeName").get
    Await.result(sphereClient.execute(ProductTypeQuery.of().byName(typeName)), 10 seconds).getResults.get(0)
  }
}

class MockProductTypes(productTypeDrafts: ProductTypeDrafts) extends ProductTypes {
  lazy val demand: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
  lazy val offer: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.offer).build()
}