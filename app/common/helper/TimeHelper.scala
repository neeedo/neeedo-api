package common.helper

import org.joda.time.{DateTimeZone, DateTime}

class TimeHelper {
  def now = new DateTime(DateTimeZone.forID("Europe/Berlin"))
}
