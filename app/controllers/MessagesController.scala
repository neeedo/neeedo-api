package controllers

import common.domain.{UserId, MessageDraft}
import common.helper.{ControllerUtils, SecuredAction}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import model.{Message, MessageId}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.MessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class MessagesController(service: MessageService, securedAction: SecuredAction) extends Controller with ControllerUtils {

  def createMessage() = securedAction.async { implicit request =>
    val maybeDraft = bindRequestJsonBody(request)(MessageDraft.messageDraftReads)

    maybeDraft match {
      case Success(draft) => service.createMessage(draft) map {
        message => Created(Json.obj("message" -> Json.toJson(message)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }


  def getMessagesForUsers(u1: UserId, u2: UserId) = SecuredAction.async {
    service.getMessagesForUsers(u1, u2) map {
      messages: List[Message] => Ok(Json.obj("messages" -> Json.toJson(messages)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def markMessageRead(id: MessageId) = SecuredAction.async {
    service.markMessageRead(id) map {
      messageId: MessageId => Ok(Json.toJson(messageId))
    } recover {
      case e: Exception => e.asResult
    }
  }

}
