package common.domain

import play.api.libs.json.{Json, Writes}
import play.api.mvc.PathBindable

case class ImageId(value: String) extends AnyVal

object ImageId {

  implicit def pathBindable: PathBindable[ImageId] = new PathBindable[ImageId] {
    override def bind(key: String, value: String): Either[String, ImageId] = {
      ImageId(value) match {
        case imageId: ImageId => Right(imageId)
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, imageId: ImageId): String = imageId.value
  }

  implicit val imageIdWrites = new Writes[ImageId] {
    def writes(i: ImageId) = Json.obj("image" -> i.value)
  }
}