package model

import java.util.UUID

import common.domain.{MessageDraft, UserId}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.PathBindable

case class Message(id: MessageId, senderId: UserId, recipientId: UserId,
  body: String, timestamp: Long, read: Boolean)

object Message extends MessageImplicits {

  def apply(draft: MessageDraft): Message = {
    Message(
      MessageId(UUID.randomUUID.toString),
      draft.senderId,
      draft.recipientId,
      draft.body,
      System.currentTimeMillis,
      false
    )
  }
}


case class MessageId(value: String)

object MessageId {

  implicit def pathBindable: PathBindable[MessageId] = new PathBindable[MessageId] {
    override def bind(key: String, value: String): Either[String, MessageId] = {
      MessageId(value) match {
        case messageId: MessageId => Right(messageId)
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, messageId: MessageId): String = messageId.value
  }

  implicit val messageIdWrites = new Writes[MessageId] {
    def writes(m: MessageId) = Json.obj("messageId" -> m.value)
  }
}
