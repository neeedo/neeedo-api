package migrations

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import common.helper.ConfigLoader
import common.logger.MigrationsLogger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmazonS3Migrations(configLoader: ConfigLoader) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# AmazonS3 Migrations started")
    val accessKey = configLoader.getString("amazonaws.accessKey")
    val secretKey = configLoader.getString("amazonaws.secretKey")
    val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
    val s3Client = new AmazonS3Client(awsCredentials)

    val bucketName = configLoader.getString("amazonaws.s3.imageBucket")

    //Todo this will not work since aws might have a pending operation on this resource
    //Todo if for example the bucket was just recently deleted
    if(!s3Client.doesBucketExist(bucketName)) {
      s3Client.createBucket(bucketName)
      MigrationsLogger.info(s"-> Created bucket $bucketName")
    }
    else {
      MigrationsLogger.info(s"-> Found bucket $bucketName")
    }

    Future(MigrationsLogger.info("# AmazonS3 Migrations finished"))
  }

}
