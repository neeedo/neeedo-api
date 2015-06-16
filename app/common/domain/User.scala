package common.domain

import io.sphere.sdk.customers.Customer
import play.api.mvc.PathBindable


case class User(id: UserId, version: Version, username: Username, email: Email)

object User extends UserImplicits {

  def fromCustomer(c: Customer): User =
    User(UserId(c.getId), Version(c.getVersion), Username(c.getFirstName), Email(c.getEmail))

}

case class Username(value: String) extends AnyVal
case class UserId(value: String)
case class UserIdAndName(id: UserId, name: Username)

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
