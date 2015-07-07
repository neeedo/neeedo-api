package model

import common.domain.{Username, UserId, UserIdAndName}
import common.helper.UUIDHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class MessageImplicitsSpec extends Specification {

  val uuid = new UUIDHelper

  val message = Message(
    MessageId(uuid.random),
    UserIdAndName(
      UserId(uuid.random),
      Username("sender_name")
    ),
    UserIdAndName(
      UserId(uuid.random),
      Username("recipient_name")
    ),
    "body",
    System.currentTimeMillis(),
    false
  )

  val messageJson = Json.obj(
    "id" -> message.id.value,
    "sender" -> Json.obj(
      "id" -> message.sender.id.value,
      "name" -> message.sender.name.value
    ),
    "recipient" -> Json.obj(
      "id" -> message.recipient.id.value,
      "name" -> message.recipient.name.value
    ),
    "body" -> message.body,
    "timestamp" -> message.timestamp,
    "read" -> message.read
  )

  "MessageImplicits.reads" should {
    "read Message from json" in {
      messageJson.as[Message] mustEqual message
    }
  }

  "MessageImplicits.writes" should {
    "write json from Message" in {
      Json.toJson(message) mustEqual messageJson
    }
  }
}
