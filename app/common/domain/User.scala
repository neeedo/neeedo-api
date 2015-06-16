package common.domain

import io.sphere.sdk.customers.Customer
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}
import play.api.mvc.PathBindable

case class User(id: UserId, version: Version, username: Username, email: Email)
object User extends UserImplicits {
  def fromCustomer(c: Customer): User =
    User(UserId(c.getId), Version(c.getVersion), Username(c.getFirstName), Email(c.getEmail))
}

case class UserIdAndName(id: UserId, name: Username)
object UserIdAndName {
  def fromCustomer(c: Customer): UserIdAndName =
    UserIdAndName(UserId(c.getId), Username(c.getFirstName))

  implicit val reads: Reads[UserIdAndName] = (
    (JsPath \ "userIdAndName" \ "id").read[String] and
    (JsPath \ "userIdAndName" \ "name").read[String]) {
      (id, username) => UserIdAndName(UserId(id), Username(username))
    }

  implicit val writes = new Writes[UserIdAndName] {
    def writes(u: UserIdAndName) = Json.obj(
      "userIdAndName" -> Json.obj(
        "id" -> u.id.value,
        "name" -> u.name.value
      )
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
