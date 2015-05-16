package common.domain

import io.sphere.sdk.models.{Image => SphereImage, ImageDimensions}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

case class ExternalImage(url: Url, width: Int, height: Int)

object ExternalImage {

  // TODO Resolution Values, add fromImage to SphereImage Object
  def toSphereImage(img: ExternalImage) = SphereImage.of(img.url.value, ImageDimensions.of(img.width, img.height))

  def fromSphereImage(img: SphereImage) = ExternalImage(Url(img.getUrl), img.getWidth, img.getHeight)

  implicit val imageReads: Reads[ExternalImage] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "width").read[Int] and
    (JsPath \ "height").read[Int]
    ) { (url, width, height) => ExternalImage(Url(url), width, height) }

  implicit val imageWrites = new Writes[ExternalImage] {
    def writes(i: ExternalImage) = Json.obj(
      "url" -> i.url.value,
      "width" -> i.width,
      "height" -> i.height
    )
  }
}
