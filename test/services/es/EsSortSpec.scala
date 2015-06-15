package services.es

import common.domain.{Latitude, Longitude, Location}
import common.helper.TimeHelper
import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import org.specs2.mutable.Specification
import play.api.libs.json.{JsArray, Json}

class EsSortSpec extends Specification with Mockito {

  trait EsSortContext extends Scope {
    val timeStamp = 1434272348084L
    val timeHelperMock = mock[TimeHelper]
    timeHelperMock.now returns new DateTime(DateTimeZone.forID("Europe/Berlin")).withMillis(timeStamp)
    val esSort = new EsSort(timeHelperMock) {}
  }

  "EsSortSpec" should {
    "buildFunctionScoredQuery with Location must return correct FunctionScoreQuery" in new EsSortContext {
      val query = esSort.buildFunctionScoredQuery(Some(Location(Longitude(1.0), Latitude(2.0))))

      Json.parse(query.toString) must beEqualTo(
        Json.obj("function_score" -> Json.obj("functions" ->
          JsArray(Seq(
            Json.obj("gauss" ->
              Json.obj("createdAt" ->
                Json.obj(
                  "origin" -> "2015-06-14T10:59:08.084+02:00",
                  "scale" -> "8h",
                  "decay" -> 0.75,
                  "offset" -> "4h"))),
            Json.obj("gauss" ->
              Json.obj("location" ->
                Json.obj(
                  "origin" -> Json.obj("lat" -> 2.0, "lon" -> 1.0),
                  "scale" -> "10km",
                  "decay" -> 0.9,
                  "offset" -> "30km")))
          ))
        ))
      )
    }

    "buildFunctionScoredQuery without Location must return correct FunctionScoreQuery" in new EsSortContext {
      val query = esSort.buildFunctionScoredQuery(None)

      Json.parse(query.toString) must beEqualTo(
        Json.obj("function_score" -> Json.obj("functions" ->
          JsArray(Seq(
            Json.obj("gauss" ->
              Json.obj("createdAt" ->
                Json.obj(
                  "origin" -> "2015-06-14T10:59:08.084+02:00",
                  "scale" -> "8h",
                  "decay" -> 0.75,
                  "offset" -> "4h")))
          ))
        ))
      )
    }
  }
}
