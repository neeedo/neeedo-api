package common.domain

case class UserCredentials(email: Email, password: String)

case class Email(value: String) extends AnyVal
