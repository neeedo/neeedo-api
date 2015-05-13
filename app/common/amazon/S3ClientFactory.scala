package common.amazon

import common.helper.ConfigLoader

class S3ClientFactory(configloader: ConfigLoader) {
  lazy val instance = getInstance

  def apply(): S3Client = instance

  def getInstance = new RemoteS3Client(configloader)

}
