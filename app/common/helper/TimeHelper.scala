package common.helper

import org.elasticsearch.common.joda.time.DateTimeZone
import org.joda.time.DateTime

class TimeHelper {
  def now = new DateTime(DateTimeZone.forID("Europe/Berlin"))
}
