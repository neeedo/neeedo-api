package model

import common.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait MessageImplicits {

  implicit val messageReads: Reads[Message] = (
      (JsPath \ "id").read[String] and
      (JsPath \ "senderId").read[String] and
      (JsPath \ "recipientId").read[String] and
      (JsPath \ "body").read[String] and
      (JsPath \ "timestamp").read[Long] and
      (JsPath \ "read").read[Boolean]
    ) {
    (id, sid, rid, body, timestamp, read) =>
      Message(
        MessageId(id),
        UserId(sid),
        UserId(rid),
        body,
        timestamp,
        read
      )
  }

  implicit val messageWrites = new Writes[Message] {
    def writes(m: Message) = Json.obj(
      "id" -> m.id.value,
      "senderId" -> m.senderId.value,
      "recipientId" -> m.recipientId.value,
      "body" -> m.body,
      "timestamp" -> m.timestamp,
      "read" -> m.read
    )
  }

}
