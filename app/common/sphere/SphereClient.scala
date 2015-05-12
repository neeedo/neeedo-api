package common.sphere

import common.helper.ConfigLoader
import io.sphere.sdk.client._

sealed trait SphereClient {
  lazy val client = createSphereClient()
  def createSphereClient(): ScalaClient

  def execute[T](req: SphereRequest[T]) = client.execute(req)
}

class RemoteSphereClient(configloader: ConfigLoader) extends SphereClient {
  override def createSphereClient(): ScalaClient = {
    val config = SphereApiConfig.of(configloader.getString("sphere.project"))
    val httpClient = NingHttpClientAdapter.of()
    val tokenSupplier = SphereAccessTokenSupplierFactory.of()
      .createSupplierOfAutoRefresh(SphereAuthConfig.of(
        configloader.getString("sphere.project"),
        configloader.getString("sphere.clientId"),
        configloader.getString("sphere.clientSecret")
    ))

    ScalaClient(SphereClient.of(config, httpClient, tokenSupplier))
  }
}
