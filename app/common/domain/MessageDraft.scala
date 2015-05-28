package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

case class MessageDraft(senderId: UserId, recipientId: UserId, body: String)

object MessageDraft {

  implicit val messageDraftReads: Reads[MessageDraft] = (
      (JsPath \ "senderId").read[String] and
      (JsPath \ "recipientId").read[String] and
      (JsPath \ "body").read[String]
    ) {
    (sid, rid, body) =>
      MessageDraft(
        UserId(sid),
        UserId(rid),
        body
      )
  }

  implicit val messageDraftWrites = new Writes[MessageDraft] {
    def writes(m: MessageDraft) = Json.obj(
      "senderId" -> m.senderId.value,
      "recipientId" -> m.recipientId.value,
      "body" -> m.body
    )
  }
}
