package common.domain

import io.sphere.sdk.models.Image
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

case class ExternalImage(url: Url, width: Int, height: Int)

object ExternalImage {

  // TODO Resolution Values
  def toSphereImage(img: ExternalImage) = Image.ofWidthAndHeight(img.url.value, img.width, img.height, "")

  def fromSphereImage(img: Image) = ExternalImage(Url(img.getUrl), img.getWidth, img.getHeight)

  implicit val externalImageReads: Reads[ExternalImage] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "width").read[Int] and
    (JsPath \ "height").read[Int]
    ) { (url, width, height) => ExternalImage(Url(url), width, height) }

  implicit val externalImageWrites = new Writes[ExternalImage] {
    def writes(i: ExternalImage) = Json.obj(
      "url" -> i.url.value,
      "width" -> i.width,
      "height" -> i.height
    )
  }
}