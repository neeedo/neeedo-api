package common.domain

import io.sphere.sdk.models.Image
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

case class ExternalImage(url: Url, label: String)

object ExternalImage {

  // TODO Resolution Values
  def toSphereImage(img: ExternalImage) = Image.ofWidthAndHeight(img.url.value, 1280, 720, img.label)

  implicit val externalImageReads: Reads[ExternalImage] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "label").read[String]
    ) { (url, label) => ExternalImage(Url(url), label) }

  implicit val externalImageWrites = new Writes[ExternalImage] {
    def writes(i: ExternalImage) = Json.obj(
      "url" -> i.url.value,
      "label" -> i.label
    )
  }
}