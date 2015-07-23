package common

import com.softwaremill.macwire.MacwireMacros._
import controllers.{StaticController, Assets}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n._
import play.api.routing.Router
import router.Routes


class NeeedoApplicationLoader extends  ApplicationLoader {

  def load(context: Context): Application = {
    val components = new BuiltInComponentsFromContext(context) with NeeedoComponents

    Play.maybeApplication match {
      case Some(app) if app.mode != Mode.Test => components.onStart()
    }

    play.api.Logger.configure(context.environment)
    components.application
  }
}

trait NeeedoComponents extends BuiltInComponents with NeeedoModule with I18nComponents {
  lazy val staticController = wire[StaticController]
  lazy val assets: Assets = wire[Assets]
  lazy val router: Router = wire[Routes].withPrefix("/")
}