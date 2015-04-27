package common.domain

import play.api.mvc.PathBindable

case class UserCredentials(email: Email, password: String)

case class Email(value: String) extends AnyVal

object Email {

  implicit def pathBindable: PathBindable[Email] = new PathBindable[Email] {
    override def bind(key: String, value: String): Either[String, Email] = {
      Email(value) match {
        case email: Email => Right(email)
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, email: Email): String = email.value
  }
}
