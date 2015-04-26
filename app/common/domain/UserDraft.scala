package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class UserDraft(username: Username, email: Email, password: String)

object UserDraft {

  implicit val userDraftReads: Reads[UserDraft] = (
    (JsPath \ "username").read[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "password").read[String]
    ) {
    (username, email, password) =>
    UserDraft(
      Username(username),
      Email(email),
      password
    )
  }

  implicit val offerDraftWrites = new Writes[UserDraft] {
    def writes(u: UserDraft) = Json.obj(
      "username" -> u.username.value,
      "email" -> u.email.value,
      "password" -> u.password
    )
  }
}
