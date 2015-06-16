package controllers

import java.util

import common.domain.{MessageDraft, UserId}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.{MessageId, Message}
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.es.EsMessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class MessagesController(esMessageService: EsMessageService, securedAction: SecuredAction) extends Controller with ControllerUtils {

  def createMessage() = securedAction.async { implicit request =>
    val maybeDraft = bindRequestJsonBody(request)(MessageDraft.messageDraftReads)

    maybeDraft match {
      case Success(draft) => esMessageService.createMessage(draft) map {
        message => Created(Json.obj("message" -> Json.toJson(message)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }

  def getMessagesByUsers(u1: UserId, u2: UserId) = securedAction.async {
    esMessageService.getMessagesByUsers(u1, u2) map {
      messages: List[Message] => Ok(Json.obj("messages" -> Json.toJson(messages)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def getConversationsByUser(id: UserId) = Action.async {
    esMessageService.getConversationsByUser(id) map {
      res => Ok("")
    } recover {
      case e: Exception => e.asResult
    }
  }

  def markMessageRead(id: MessageId) = securedAction.async {
    esMessageService.markMessageRead(id) map {
      case Some(messageId) => Ok(Json.toJson(messageId))
      case None => NotFound
    } recover {
      case e: Exception => e.asResult
    }
  }

}
