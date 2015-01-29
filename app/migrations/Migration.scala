package migrations

import scala.concurrent.Future

trait Migration {
  def run() : Future[Unit]
}
