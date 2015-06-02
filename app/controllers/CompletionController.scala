package controllers

import common.domain.{CompletionPhrase, CompletionTag}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.CompletionService
import scala.concurrent.ExecutionContext.Implicits.global
import common.helper.ImplicitConversions.ExceptionToResultConverter

class CompletionController(completionService: CompletionService) extends Controller {
  def completeTag(tag: String) = Action.async {
    completionService.completeTag(CompletionTag(tag)) map {
      res => Ok(Json.toJson(res))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def suggestTags(phrase: String) = Action.async {
    completionService.suggestTags(CompletionPhrase(phrase)) map {
      res => Ok(Json.toJson(res))
    } recover {
      case e: Exception => e.asResult
    }
  }
}
