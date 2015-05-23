package migrations

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.policy.actions.S3Actions
import com.amazonaws.auth.policy.resources.S3ObjectResource
import com.amazonaws.auth.policy.{Policy, Principal, Statement}
import com.amazonaws.auth.policy.Statement.Effect
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import common.helper.ConfigLoader
import common.logger.MigrationsLogger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmazonS3Migrations(configLoader: ConfigLoader) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# AmazonS3 Migrations started")
    val accessKey = configLoader.getString("aws.accessKeyId")
    val secretKey = configLoader.getString("aws.secretKey")
    val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
    val s3Client = new AmazonS3Client(awsCredentials)

    val bucketName = configLoader.getString("aws.s3.imageBucket")

    //Todo this will not work since aws might have a pending operation on this resource
    //Todo if for example the bucket was just recently deleted
    if(!s3Client.doesBucketExist(bucketName)) {
      val allowPublicReadStatement = new Statement(Effect.Allow)
        .withPrincipals(Principal.AllUsers)
        .withActions(S3Actions.GetObject)
        .withResources(new S3ObjectResource(bucketName, "*"));

      val policy = new Policy().withStatements(allowPublicReadStatement)
      s3Client.createBucket(bucketName)
      s3Client.setBucketPolicy(bucketName, policy.toJson)
      MigrationsLogger.info(s"-> Created bucket $bucketName")
    }
    else {
      MigrationsLogger.info(s"-> Found bucket $bucketName")
    }

    Future(MigrationsLogger.info("# AmazonS3 Migrations finished"))
  }

}
