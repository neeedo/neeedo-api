package common.sphere

import common.helper.ConfigLoader

class SphereClientFactory(configloader: ConfigLoader) {
  lazy val instance = getInstance

  def apply(): SphereClient = instance

  def getInstance = new RemoteSphereClient(configloader)
}
