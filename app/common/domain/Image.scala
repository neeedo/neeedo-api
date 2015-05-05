package common.domain

import io.sphere.sdk.models.Image

case class ExternalImage(url: Url, label: String)

object ExternalImage {
  def toSphereImage(img: ExternalImage) = Image.ofWidthAndHeight(img.url.value, 1280, 720, img.label)
}