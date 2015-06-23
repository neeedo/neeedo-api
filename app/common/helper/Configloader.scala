package common.helper

import common.domain.IndexName
import common.exceptions.InvalidConfiguration
import common.logger.ConfigLogger
import play.api.{Logger, Configuration}

import scala.util.{Failure, Success, Try}

class ConfigLoader(config: Configuration) {
  def getStringSeq(key: String): List[String] = getStringSeq(key, ",")
  def getStringSeq(key: String, delimiter: String): List[String] = config
      .getString(key)
      .getOrElse(throwIllegalConfigException(key))
      .split(delimiter).toList

  def getStringOpt(key: String): Option[String] = config.getString(key)

  def getString(key: String): String = config.getString(key).getOrElse {
    throwIllegalConfigException(key)
  }
  def getInt(key: String) = getString(key).toInt

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

  lazy val demandIndex = readIndexFromConfig("demand.typeName")
  lazy val offerIndex = readIndexFromConfig("offer.typeName")
  lazy val completionsIndex = readIndexFromConfig("completionsIndexName")
  lazy val messagesIndex = readIndexFromConfig("messagesIndexName")
  lazy val favoritesIndex = readIndexFromConfig("favoritesIndexName")

  def throwIllegalConfigException(key: String): Nothing = {
    val ex = new InvalidConfiguration(s"Missing Config Key: '$key'! " +
      s"Please check your Neeedo API Configuartion. " +
      s"Does your custom-application.conf file exist?")
    ConfigLogger.error("Missing Config Key", ex)
    throw ex
  }
}
