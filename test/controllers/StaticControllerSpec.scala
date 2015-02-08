package controllers

import common.helper.CrossOriginFilter
import org.specs2.mutable.Specification
import play.api.test.{Helpers, FakeRequest}
import play.api.test.Helpers.defaultAwaitTimeout

class StaticControllerSpec extends Specification {

  "deliverCorsHeaders" should {
    "deliver correct headers" in {
      val controller = new Static
      val result = CrossOriginFilter.apply(controller.deliverCorsHeaders())(FakeRequest("OPTIONS", "/demands")).run

      Helpers.status(result) must equalTo(200)
      Helpers.headers(result) mustEqual Map(
        "Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
        "Access-Control-Allow-Headers" -> "content-type, accept, origin",
        "Access-Control-Max-Age" -> "86400"
      )
    }
  }
}
