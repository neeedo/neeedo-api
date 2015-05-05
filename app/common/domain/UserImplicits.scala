package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait UserImplicits {

  implicit val userReads: Reads[User] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "username").read[String] and
    (JsPath \ "email").read[String]
    ) {
    (uid, version, username, email) =>
      User(
        UserId(uid),
        Version(version),
        Username(username),
        Email(email)
      )
  }

  implicit val userWrites = new Writes[User] {
    def writes(u: User) = Json.obj(
      "id" -> u.id.value,
      "version" -> u.version.value,
      "username" -> u.username.value,
      "email" -> u.email.value
    )
  }
}
