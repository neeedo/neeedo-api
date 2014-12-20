package common.helper

import play.api.Play

import scala.collection.JavaConversions._

object Configloader {
  def getStringSeq(key: String): Option[List[String]] = Play.current.configuration.getStringList(key).map(_.toList)
  def getStringOpt(key: String): Option[String] = Play.current.configuration.getString(key)
  def getBoolean(key: String) = Play.current.configuration.getBoolean(key).getOrElse(false)
}
