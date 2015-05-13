package services

import common.amazon.S3Client
import common.domain.ImageHash

import scala.reflect.io.File

class ImageService(s3Client: AmazonS3Client) {

  def uploadImage(image: File) = ???

  def deleteImage(imageHash: ImageHash) = ???

}
