package common.domain

import play.api.libs.json.{Json, Writes}
import play.api.mvc.PathBindable

case class User(id: UserId, version: Version, username: Username, email: Email)
object User extends UserImplicits

case class UserIdAndName(id: UserId, name: Username)
object UserIdAndName {
  implicit val writes = new Writes[UserIdAndName] {
    def writes(u: UserIdAndName) = Json.obj(
      "id" -> u.id.value,
      "name" -> u.name.value
    )
  }
}

case class UserId(value: String)
object UserId {
  implicit def pathBindable: PathBindable[UserId] = new PathBindable[UserId] {
    override def bind(key: String, value: String): Either[String, UserId] = {
      UserId(value) match {
        case userId: UserId => Right(userId)
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, userId: UserId): String = userId.value
  }
}

case class Username(value: String) extends AnyVal
