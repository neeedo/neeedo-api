package common.sphere

import common.helper.Configloader
import io.sphere.sdk.client._

sealed trait SphereClient {
  lazy val client = createSphereClient()
  def createSphereClient(): ScalaClient

  def execute[T](req: SphereRequest[T]) = client.execute(req)
}

object RemoteSphereClient extends SphereClient {
  override def createSphereClient(): ScalaClient = {
//    val config = Play.current.configuration.underlying
    val config = SphereApiConfig.of(Configloader.getString("sphere.project"))
    val httpClient = NingHttpClientAdapter.of()
    val tokenSupplier = SphereAccessTokenSupplierFactory.of()
      .createSupplierOfAutoRefresh(SphereAuthConfig.of(
        Configloader.getString("sphere.project"),
        Configloader.getString("sphere.clientId"),
        Configloader.getString("sphere.clientSecret")
    ))

    ScalaClient(SphereClient.of(config, httpClient, tokenSupplier))
  }





}
