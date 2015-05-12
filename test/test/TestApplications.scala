package test

import play.api.test.{FakeApplication, WithApplication}

object TestApplications {
  def loggingOffApp(additionalConfig: Map[String,  _ <: Any] = Map.empty) =
    new WithApplication(
      FakeApplication(
        additionalConfiguration = Map(
          "logger.application" -> "OFF",
          "logger.Migrations" -> "OFF",
          "logger.Elasticsearch" -> "OFF",
          "logger.Offer" -> "OFF",
          "offer.typeName" -> "offer",
          "demand.typeName" -> "demand"
        ) ++ additionalConfig)){}
  def configOffApp(additionalConfig: Map[String,  _ <: Any] = Map.empty) = new WithApplication(FakeApplication(additionalConfiguration = additionalConfig)){}
}
