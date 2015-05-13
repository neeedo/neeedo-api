package common.amazon

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import common.helper.ConfigLoader

sealed trait S3Client {
  lazy val client = createClient()
  def createClient(): AmazonS3Client
}

class RemoteS3Client(configLoader: ConfigLoader) extends S3Client {
  override def createClient(): AmazonS3Client = {
    val accessKey = configLoader.getString("amazonaws.accessKey")
    val secretKey = configLoader.getString("amazonaws.secretKey")
    val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
    new AmazonS3Client(awsCredentials)
  }
}