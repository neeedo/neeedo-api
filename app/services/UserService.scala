package services

import common.domain._
import common.sphere.SphereClient
import scala.concurrent.Future

class UserService(sphereClient: SphereClient) {

  def getUserByName(username: Username): Future[Option[User]] = Future.successful(None)
  def createUser(userDraft: UserDraft): Future[Option[User]] = Future.successful(None)
  def updateUser(id: UserId, version: Version, userDraft: UserDraft): Future[Option[User]] = Future.successful(None)
  def deleteUser(id: UserId, version: Version, userDraft: UserDraft): Future[Option[User]] = Future.successful(None)

  def writeUserToSphere(draft: UserDraft): Future[Option[User]] = Future.successful(None)

}
