package common.domain

import io.sphere.sdk.models.{Image => SphereImage, ImageDimensions}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

case class Image(url: Url, width: Int, height: Int)

object Image {

  // TODO Resolution Values, add fromImage to SphereImage Object
  def toSphereImage(img: Image) = SphereImage.of(img.url.value, ImageDimensions.of(img.width, img.height))

  def fromSphereImage(img: SphereImage) = Image(Url(img.getUrl), img.getWidth, img.getHeight)

  implicit val imageReads: Reads[Image] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "width").read[Int] and
    (JsPath \ "height").read[Int]
    ) { (url, width, height) => Image(Url(url), width, height) }

  implicit val imageWrites = new Writes[Image] {
    def writes(i: Image) = Json.obj(
      "url" -> i.url.value,
      "width" -> i.width,
      "height" -> i.height
    )
  }
}