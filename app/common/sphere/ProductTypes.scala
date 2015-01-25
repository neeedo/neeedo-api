package common.sphere

import common.helper.Configloader
import io.sphere.sdk.producttypes.queries.ProductTypeQuery

import scala.concurrent.Await
import scala.concurrent.duration._

object ProductTypes {
  private lazy val sphereClient = SphereClientFactory.getInstance

  def demand = {
    val typeName = Configloader.getStringOpt("demand.typeName").get
    Await.result(sphereClient.execute(ProductTypeQuery.of().byName(typeName)), 10 seconds).getResults.get(0)
  }

  def offer = {
    val typeName = Configloader.getStringOpt("offer.typeName").get
    Await.result(sphereClient.execute(ProductTypeQuery.of().byName(typeName)), 10 seconds).getResults.get(0)
  }
}
