package common.domain

import java.nio.charset.StandardCharsets

import play.api.mvc.PathBindable

case class Email(private val in: String) {
  val value = play.utils.UriEncoding.decodePath(in, StandardCharsets.UTF_8.toString)
}

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