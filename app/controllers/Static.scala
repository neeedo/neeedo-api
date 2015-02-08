package controllers

import play.api.mvc.{Action, Controller}

class Static extends Controller {
  def status = Action { Ok("Neeedo-API up and running.") }
  def deliverCorsHeaders() = Action {Ok}
}
