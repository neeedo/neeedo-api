package common.domain

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class UserIdAndNameSpec extends Specification {

  trait UserIdAndNameContext extends Scope {
    val userIdAndName = UserIdAndName(UserId("123-id"), Username("Blubbster"))
    val userIdAndNameJson = Json.obj("id" -> "123-id", "name" -> "Blubbster")
  }

  "UserIdAndName" should {
    "UserIdAndName should parse into correct json" in new UserIdAndNameContext {
      Json.toJson(userIdAndName) must be equalTo userIdAndNameJson
    }
  }
}
