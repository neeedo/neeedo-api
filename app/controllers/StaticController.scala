package controllers

import play.api.mvc.{Action, Controller}

class StaticController extends Controller {
  def status = Action { Ok("Neeedo-API up and running.") }

  def deliverCorsHeaders() = Action { Ok }

  def showDocumentation = Action {
    Redirect("https://github.com/neeedo/neeedo-api#readme")
  }
}
