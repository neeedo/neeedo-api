package controllers

import play.api.mvc._

class Documentation extends Controller {

  def showDocumentation = Action {
    Redirect("https://github.com/HTW-Projekt-2014-Commercetools/api/blob/master/README.md")
  }
}
