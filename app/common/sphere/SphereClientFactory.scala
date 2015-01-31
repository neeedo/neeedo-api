package common.sphere

object SphereClientFactory {
  lazy val instance = getInstance

  def apply(): SphereClient = instance

  def getInstance = RemoteSphereClient
}
