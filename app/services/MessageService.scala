package services

import common.domain.{UserId, MessageDraft}
import model.{MessageId, Message}
import services.es.EsMessageService

import scala.concurrent.Future

class MessageService(esMessageService: EsMessageService) {

  def createMessage(draft: MessageDraft): Future[Message] =
    esMessageService.createMessage(Message(draft))

  def markMessageRead(id: MessageId): Future[MessageId] =
    esMessageService.markMessageRead(id)

  def getMessagesForUsers(u1: UserId, u2: UserId): Future[List[Message]] = {
    esMessageService.getMessagesForUsers(u1, u2)
  }

}
