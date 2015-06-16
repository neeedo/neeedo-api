package common.domain

import play.api.mvc.QueryStringBindable

case class Location(lon: Longitude, lat: Latitude)
case class Longitude(value: Double) extends AnyVal {
//TODO  require(value > -180, "Longitude must be between -180 and 180")
//TODO  require(value >= 180, "Longitude must be between -180 and 180")
}
case class Latitude(value: Double) extends AnyVal {
//TODO  require(value > -90, "Latitude must be between -90 and 90")
//TODO  require(value >= 90, "Latitude must be between -90 an 90")
}

object Location {
  implicit def queryStringBinder(implicit doubleBinder: QueryStringBindable[Double]) = new QueryStringBindable[Location] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Location]] = {
      for {
        lon <- doubleBinder.bind("lon", params)
        lat <- doubleBinder.bind("lat", params)
      } yield {
        (lon, lat) match {
          case (Right(longitude), Right(latitude)) => Right(Location(Longitude(longitude), Latitude(latitude)))
          case _ => Left("Unable to bind a Location")
        }
      }
    }
    override def unbind(key: String, location: Location): String = {
      doubleBinder.unbind("lon", location.lon.value) + "&" + doubleBinder.unbind("lat", location.lat.value)
    }
  }
}
