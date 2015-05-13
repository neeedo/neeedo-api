package common.helper

import common.domain.IndexName
import common.exceptions.InvalidConfiguration
import play.api.Play

import scala.util.{Failure, Success, Try}

class ConfigLoader {
  private val config = Play.current.configuration

  def getStringSeq(key: String): List[String] = getStringSeq(key, ",")
  def getStringSeq(key: String, delimiter: String): List[String] = config
      .getString(key)
      .getOrElse(throwIllegalConfigException(key))
      .split(delimiter).toList

  def getStringOpt(key: String): Option[String] = config.getString(key)

  def getString(key: String): String = config.getString(key).getOrElse {
    throwIllegalConfigException(key)
  }

  def getBoolean(key: String): Boolean = config.getBoolean(key).getOrElse {
    throwIllegalConfigException(key)
  }

  def readIndexFromConfig(key: String): IndexName = {
    Try {
      getString(key)
    } match {
      case Success(i) => IndexName(i)
      case Failure(e) => throw e
    }
  }

  val demandIndex = readIndexFromConfig("demand.typeName")
  val offerIndex = readIndexFromConfig("offer.typeName")
  val completionsIndex = readIndexFromConfig("completionsIndexName")

  def throwIllegalConfigException(key: String): Nothing = {
    throw new InvalidConfiguration(s"Missing Config Key: '$key'! " +
      s"Please check your Neeedo API Configuartion. " +
      s"Does your custom-application.conf file exist?")
  }
}
