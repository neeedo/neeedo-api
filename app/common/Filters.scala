package common

import common.helper.CrossOriginFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

class Filters extends HttpFilters {
  override def filters: Seq[EssentialFilter] = CrossOriginFilter :: Nil
}
