package model

import common.domain._
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.PathBindable
import play.api.test.WithApplication

class DemandSpec extends Specification {

  val demandJs: JsObject = Json.obj(
    "id" -> "testId",
    "version" -> 1L,
    "userId" -> "testUid",
    "tags" -> "testTags",
    "location" -> Json.obj(
      "lat" -> 10.0,
      "lon" -> 20.0
    ),
    "distance" -> 1,
    "price" -> Json.obj(
      "min" -> 50.0,
      "max" -> 100.0
    )
  )

  val demand: Demand = Demand(
    DemandId("testId"),
    Version(1L),
    UserId("testUid"),
    "testTags",
    Location(Longitude(20.0), Latitude(10.0)),
    Distance(1),
    Price(50.0),
    Price(100.0))

  "Demand" should {
    "demand json should be correctly parsed into a demand object" in new WithApplication {
      demandJs.as[Demand] must beEqualTo(demand)
    }

    "demand object should be correctly parsed into a demand json" in new WithApplication {
      Json.toJson(demand) must beEqualTo(demandJs)
    }
  }

  "DemandId" should {
    "be correctly be created from an identifier" in new WithApplication {
      DemandId.pathBinder.bind("key1", "12345abc") mustEqual Right(DemandId("12345abc"))
    }

    "be correctly be transforem into an identifier" in new WithApplication {
      DemandId.pathBinder.unbind("key", DemandId("abcd")) mustEqual("abcd")
    }
  }

}
