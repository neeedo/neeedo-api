package services

import common.domain._
import common.helper.Wirehelper
import common.sphere.SphereClient
import io.sphere.sdk.customers.queries.{CustomerByIdFetch, CustomerQuery}
import io.sphere.sdk.customers.{CustomerDraft, CustomerName}
import io.sphere.sdk.customers.commands.{CustomerCreateCommand, CustomerUpdateCommand}
import scala.concurrent.Future
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

class UserService(sphereClient: SphereClient) {

  def getUserByName(username: Username): Future[Option[User]] = Future.successful(None)

  def getUserByEmail(email: String) = Future.successful(None)

  def getUserById(id: UserId) = {
    val customerByIdFetch = CustomerByIdFetch.of(id.value)

    sphereClient.execute(customerByIdFetch).map(User.customerToUser(_))
  }

  def createUser(userDraft: UserDraft): Future[Option[User]] = {
    val customerName = CustomerName.ofFirstAndLastName("Peter", "Gerhard")
    val customerDraft = CustomerDraft.of(customerName, "peter.gerhard90@gmail.com", "peter")
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)

    sphereClient.execute(customerCreateCommand).map(User.customerToUser(_))
  }

  def updateUser(id: UserId, version: Version, userDraft: UserDraft): Future[Option[User]] = {
    // invalidate cache for user
    Future.successful(None)
  }
  def deleteUser(id: UserId, version: Version): Future[Option[User]] = {
    // invalidate cache for user
    Future.successful(None)
  }

  def authorizeUser(userCredentials: UserCredentials): Future[Boolean] = {
    val cachedUser: Option[User] = Cache.getAs[User](s"user.${userCredentials.user.value}")
    cachedUser match {
      case Some(user) => Future.successful(user.userCredentials.pw == userCredentials.pw)
      case None =>
        getUserByName(userCredentials.user).map {
          case Some(user) =>
            if (user.userCredentials.pw == userCredentials.pw) {
              Cache.set(s"user.${userCredentials.user.value}", user)
            }
            user.userCredentials.pw == userCredentials.pw
          case None => false
        }
    }
  }
}

object UserService {
  def authorizeUser(userCredentials: UserCredentials): Future[Boolean] =
    Wirehelper.wired.lookupSingleOrThrow(classOf[UserService]).authorizeUser(userCredentials)
}
