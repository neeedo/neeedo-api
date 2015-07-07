package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait UserImplicits {

  implicit val userWrites = new Writes[User] {
    def writes(u: User) = Json.obj(
      "id" -> u.id.value,
      "version" -> u.version.value,
      "name" -> u.username.value,
      "email" -> u.email.value
    )
  }
}
