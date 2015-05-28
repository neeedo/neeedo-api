package controllers

import common.domain.MessageDraft
import common.helper.{ControllerUtils, SecuredAction}
import common.helper.ImplicitConversions.ExceptionToResultConverter
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

}
