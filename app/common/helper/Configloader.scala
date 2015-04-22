package common.helper

import play.api.Play

import scala.collection.JavaConversions._

object Configloader {
  def getStringSeq(key: String): List[String] = getStringSeq(key, ",")
  def getStringSeq(key: String, delimiter: String): List[String] =
    Play.current.configuration.getString(key).getOrElse("").split(delimiter).toList
  def getStringOpt(key: String): Option[String] = Play.current.configuration.getString(key)
  def getString(key: String) = Play.current.configuration.getString(key).get
  def getBoolean(key: String) = Play.current.configuration.getBoolean(key).getOrElse(false)
}
