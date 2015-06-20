package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait UserImplicits {

  implicit val userReads: Reads[User] = (
    (JsPath \ "user" \ "id").read[String] and
    (JsPath \ "user" \ "version").read[Long] and
    (JsPath \ "user" \ "name").read[String] and
    (JsPath \ "user" \ "email").read[String]
    ) {
    (id, version, username, email) =>
      User(
        UserId(id),
        Version(version),
        Username(username),
        Email(email)
      )
  }

  implicit val userWrites = new Writes[User] {
    def writes(u: User) = Json.obj(
      "user" -> Json.obj(
        "id" -> u.id.value,
        "version" -> u.version.value,
        "name" -> u.username.value,
        "email" -> u.email.value
      )
    )
  }
}
