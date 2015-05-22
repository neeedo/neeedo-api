package common.amazon

import java.io.File

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import common.helper.ConfigLoader
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait S3Client {
  lazy val client = createClient()
  val bucketName: String

  def createClient(): AmazonS3Client

  def getObject(key: String, dst: File) = Future(client.getObject(new GetObjectRequest(bucketName, key), dst))
  def putObject(key: String, file: File) = Future(client.putObject(bucketName, key, file))
  def deleteObject(key: String) = Future(client.deleteObject(bucketName, key))
}

class RemoteS3Client(configLoader: ConfigLoader) extends S3Client {
  val bucketName = configLoader.getString("aws.s3.imageBucket")

  override def createClient(): AmazonS3Client = {
    val accessKey = configLoader.getString("aws.accessKeyId")
    val secretKey = configLoader.getString("aws.secretKey")
    val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
    // Todo Configuration
    new AmazonS3Client(awsCredentials)
  }

}