package common.domain

import io.sphere.sdk.customers.Customer
import play.api.mvc.PathBindable

case class User(user: Customer, password: String) {
  def mail: Usermail = Usermail(user.getEmail)
}
case class UserId(value: String)
object UserId {
  implicit def pathBinder: PathBindable[UserId] = new PathBindable[UserId] {
    override def bind(key: String, value: String): Either[String, UserId] = {
      UserId.apply(value) match {
        case x: UserId => Right(x)
        //TODO version validation here!
        case _	=> Left("Bla")
      }
    }
    override def unbind(key: String, userId: UserId): String = userId.value.toString
  }
}


object Username {
  implicit def pathBinder: PathBindable[Username] = new PathBindable[Username] {
    override def bind(key: String, value: String): Either[String, Username] = {
      Username.apply(value) match {
        case x: Username => Right(x)
        case _ => Left("Bla")
      }
    }
    override def unbind(key: String, username: Username): String = username.value.toString
  }
}

case class Username(value: String) extends AnyVal
case class Usermail(value: String) extends AnyVal
case class UserCredentials(mail: Usermail, pw: String)