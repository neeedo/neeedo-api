package controllers

import play.api.mvc._

class Documentation extends Controller {

  def showDocumentation = Action {
    Redirect("https://github.com/neeedo/neeedo-api#readme")
  }
}
