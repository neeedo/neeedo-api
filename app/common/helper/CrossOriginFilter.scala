package common.helper

import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object CrossOriginFilter extends Filter {
  def apply(nextFilter: (RequestHeader) => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    nextFilter(requestHeader).map { result =>
      result.withHeaders(
        "Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
        "Access-Control-Allow-Headers" -> "content-type, accept, origin",
        "Access-Control-Max-Age" -> "86400")
    }
  }
}