package common.amazon

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.s3.AmazonS3Client

sealed trait S3Client {

  lazy val client = createClient()

  def createClient(): AmazonS3Client

}

object RemoteS3Client extends S3Client {

  override def createClient(): AmazonS3Client = {
    val client = new AmazonS3Client
    client.setRegion(Region.getRegion(Regions.US_WEST_2))
    client
  }

}