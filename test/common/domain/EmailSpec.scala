package common.domain

import org.specs2.mutable.Specification

class EmailSpec extends Specification {

  "Email" should {
    "decode encoded emails correctly" in {
      Email("test%40test.de").value must beEqualTo("test@test.de")
    }

    "decode already decoded emails correctly" in {
      Email("test@test.de").value must beEqualTo("test@test.de")
    }
  }
}
