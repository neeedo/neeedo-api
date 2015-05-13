package common.amazon

object S3ClientFactory {

  lazy val instance = getInstance

  def apply(): S3Client = instance

  private def getInstance = RemoteS3Client

}
