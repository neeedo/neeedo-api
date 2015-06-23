package common.domain

import model.OfferId
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, Json, JsPath, Reads}

case class Favorite(userId: UserId, offerId: OfferId)

object Favorite {

  implicit val reads: Reads[Favorite] = (
    (JsPath \ "userId").read[String] and
    (JsPath \ "offerId").read[String]
  ) {
    (uid, oid) => Favorite(UserId(uid), OfferId(oid))
  }

  implicit val writes = new Writes[Favorite] {
    def writes(f: Favorite) = Json.obj(
      "userId" -> f.userId.value,
      "offerId" -> f.offerId.value
    )
  }
}
