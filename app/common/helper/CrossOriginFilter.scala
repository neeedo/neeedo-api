package common.helper

import play.api.http.HeaderNames._
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object CrossOriginFilter extends Filter {
  def apply(nextFilter: (RequestHeader) => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    nextFilter(requestHeader).map { result =>
      result.withHeaders(
        ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
        ACCESS_CONTROL_ALLOW_METHODS -> "GET, POST, OPTIONS",
        ACCESS_CONTROL_ALLOW_HEADERS -> "content-type, accept, origin",
        ACCESS_CONTROL_MAX_AGE -> "86400")
    }
  }
}