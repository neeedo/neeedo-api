package common.domain

import play.api.libs.json.{Json, Writes}

case class CompletionTag(rawValue: String) {
  val value = rawValue.trim
}

case class CompletionTagResult(value: List[String])
object CompletionTagResult {
  implicit val completionTagResult = new Writes[CompletionTagResult] {
    def writes(c: CompletionTagResult) = Json.obj("completedTags" -> c.value)
  }
}

case class CompletionPhrase(value: List[String])
object CompletionPhrase {
  def apply(phrase: String) = new CompletionPhrase(phrase.split(",").toList.map(_.trim))
}

case class CompletionPhraseResult(value: List[String])
object CompletionPhraseResult {
  implicit val completionPhraseResult = new Writes[CompletionPhraseResult] {
    def writes(c: CompletionPhraseResult) = Json.obj("suggestedTags" -> c.value)
  }
}
