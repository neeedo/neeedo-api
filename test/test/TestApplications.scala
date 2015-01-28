package test

import play.api.test.{FakeApplication, WithApplication}

object TestApplications {
  def loggingOffApp = new WithApplication(FakeApplication(additionalConfiguration = Map("logger.application" -> "OFF"))){}
}
