package services

import common.domain._
import services.es.{EsCompletionService, EsSuggestionService}

import scala.concurrent.Future


class CompletionService(esCompletionService: EsCompletionService, esSuggestionService: EsSuggestionService) {

  def completeTag(tag: CompletionTag): Future[CompletionTagResult] =
    esCompletionService.getCompletions(tag)

  def suggestTags(phrase: CompletionPhrase): Future[CompletionPhraseResult] =
    esSuggestionService.getSuggestions(phrase)

  def writeCompletions(tags: List[CompletionTag]) =
    esCompletionService.upsertCompletions(tags)
}

