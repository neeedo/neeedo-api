package common.domain

import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}

class UserImplicitsSpec extends Specification {

  val userJson: JsObject = Json.obj(
      "id" -> "abcdefg",
      "version" -> 1L,
      "name" -> "test",
      "email" -> "test@test.de"
  )

  val user = User(UserId("abcdefg"), Version(1L), Username("test"), Email("test@test.de"))

  "UserImplicits.writes" should {
    "Construct correct json from User" in {
      Json.toJson(user) mustEqual userJson
    }
  }
}
