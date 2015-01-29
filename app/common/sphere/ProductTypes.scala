package common.sphere

import common.helper.Configloader
import io.sphere.sdk.attributes.AttributeDefinition
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

trait ProductTypes {
  val demand: ProductType
  val offer: ProductType
}

class SphereProductTypes(sphereClient: SphereClient) extends ProductTypes {
  val demand: ProductType = queryDemandType
  val offer: ProductType = queryOfferType

  private def queryDemandType: ProductType = {
    val typeName = Configloader.getStringOpt("demand.typeName").get
    Await.result(sphereClient.execute(ProductTypeQuery.of().byName(typeName)), 10 seconds).getResults.get(0)
  }

  private def queryOfferType: ProductType = {
    val typeName = Configloader.getStringOpt("offer.typeName").get
    Await.result(sphereClient.execute(ProductTypeQuery.of().byName(typeName)), 10 seconds).getResults.get(0)
  }
}

class MockProductTypes(sphereClient: SphereClient) extends ProductTypes {
  val demand: ProductType = ProductTypeBuilder.of("id", "name", "desc", List.empty[AttributeDefinition].asJava).build()
  val offer: ProductType = ProductTypeBuilder.of("id", "name", "desc", List.empty[AttributeDefinition].asJava).build()
}