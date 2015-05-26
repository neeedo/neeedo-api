package model

import common.domain.UserId
import play.api.mvc.PathBindable

case class Message(id: MessageId, senderId: UserId, recipientId: UserId,
  body: String, timestamp: Long, read: Boolean) extends MessageImplicits


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
}
