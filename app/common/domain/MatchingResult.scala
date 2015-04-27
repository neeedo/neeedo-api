package common.domain

import model.{Offer, Demand, CardId, Card}
import play.api.libs.json.{Json, Writes}

case class MatchingResult(hits: Long, from: From, pageSize: PageSize, results: List[Card])

object MatchingResult {
  implicit val matchingResult = new Writes[MatchingResult] {
    def writes(m: MatchingResult) = Json.obj(
      "matches" -> Json.obj(
        "total" -> m.hits,
        "from" -> m.from.value,
        "pageSize" -> m.pageSize.value,
        "matching" -> m.results.map {
          case (d: Demand) => Json.toJson(d)
          case (o: Offer) => Json.toJson(o)
        }
      )
    )
  }
}

case class EsMatchingResult(hits: Long, results: List[CardId])
