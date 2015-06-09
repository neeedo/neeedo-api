package common.domain

import model.{Offer, Demand, CardId, Card}
import play.api.libs.json.{Json, Writes}

case class MatchingResult(hits: Long, pager: Pager, results: List[Card])

object MatchingResult {
  implicit val matchingResult = new Writes[MatchingResult] {
    def writes(m: MatchingResult) = Json.obj(
      "matches" -> Json.obj(
        "total" -> m.hits,
        "offset" -> m.pager.offset,
        "limit" -> m.pager.limit,
        "matching" -> m.results.map {
          case (d: Demand) => Json.toJson(d)
          case (o: Offer) => Json.toJson(o)
        }
      )
    )
  }
}

case class EsMatchingResult(hits: Long, results: List[CardId])
