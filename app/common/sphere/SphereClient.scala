package common.sphere

import io.sphere.sdk.client.{ScalaClientImpl, ScalaClient}
import io.sphere.sdk.http.ClientRequest
import play.api.Play

sealed trait SphereClient {
  lazy val client = createSphereClient()
  def createSphereClient(): ScalaClient

  def execute[T](req: ClientRequest[T]) = client.execute(req)
}

object RemoteSphereClient extends SphereClient {
  override def createSphereClient(): ScalaClient = new ScalaClientImpl(Play.current.configuration.underlying)
}
