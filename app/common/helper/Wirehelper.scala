package common.helper

import com.softwaremill.macwire.{Wired, MacwireMacros}

object Wirehelper {
  import MacwireMacros._
  lazy val wired = wiredInModule(new WireDependencies {})

  def apply(): Wired = wired
}
