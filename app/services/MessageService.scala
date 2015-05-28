package services

import common.domain.MessageDraft
import model.Message
import services.es.EsMessageService

import scala.concurrent.Future

class MessageService(esMessageService: EsMessageService) {

  def createMessage(draft: MessageDraft): Future[Message] =
    esMessageService.createMessage(Message(draft))



}
