package model

import common.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait MessageImplicits {

  implicit val messageReads: Reads[Message] = (
      (JsPath \ "id").read[String] and
      (JsPath \ "sender" \ "id").read[String] and
      (JsPath \ "sender" \ "name").read[String] and
      (JsPath \ "recipient" \ "id").read[String] and
      (JsPath \ "recipient" \ "name").read[String] and
      (JsPath \ "body").read[String] and
      (JsPath \ "timestamp").read[Long] and
      (JsPath \ "read").read[Boolean]
    ) {
    (id, sid, sname, rid, rname, body, timestamp, read) =>
      Message(
        MessageId(id),
        UserIdAndName(
          UserId(sid),
          Username(sname)
        ),
        UserIdAndName(
          UserId(rid),
          Username(rname)
        ),
        body,
        timestamp,
        read
      )
  }

  implicit val messageWrites = new Writes[Message] {
    def writes(m: Message) = Json.obj(
      "id" -> m.id.value,
      "sender" -> Json.obj(
        "id" -> m.sender.id.value,
        "name" -> m.sender.name.value
      ),
      "recipient" -> Json.obj(
        "id" -> m.recipient.id.value,
        "name" -> m.recipient.name.value
      ),
      "body" -> m.body,
      "timestamp" -> m.timestamp,
      "read" -> m.read
    )
  }

}
