package controllers

import services.es.EsMessageService

class MessagesController(service: EsMessageService) {

  def createMessage(draft: MessageDraft) = ???
}
