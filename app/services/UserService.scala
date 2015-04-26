package services

import java.security.MessageDigest
import java.util.Optional

import common.domain._
import common.helper.Wirehelper
import common.sphere.SphereClient
import io.sphere.sdk.customers.{CustomerSignInResult, CustomerDraft, CustomerName}
import io.sphere.sdk.customers.commands.{CustomerCreateCommand, CustomerSignInCommand}
import scala.concurrent.Future
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global


class UserService(sphereClient: SphereClient) {

  def getUserByEmail(email: String) = Future.successful(None)

  def createUser(userDraft: UserDraft): Future[Option[User]] = {
    val customerName = CustomerName.ofFirstAndLastName(userDraft.username.value, "NonEmpty")
    val customerDraft = CustomerDraft.of(customerName, userDraft.email.value, userDraft.password)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)

    for { result <- sphereClient.execute(customerCreateCommand) }
      yield Option(User.fromCustomer(result.getCustomer))
  }

  def updateUser(id: UserId, version: Version, userDraft: UserDraft): Future[Option[User]] = {
    // invalidate cache for user
    Future.successful(None)
  }
  def deleteUser(id: UserId, version: Version): Future[Option[User]] = {
    // invalidate cache for user
    Future.successful(None)
  }

  def authorizeUser(credentials: UserCredentials): Future[Boolean] = {
    def md5(s: String): String = new String(MessageDigest.getInstance("MD5").digest(s.getBytes))

    val cachedUserCredentials: Option[UserCredentials] = Cache.getAs[UserCredentials](s"userCredentials.${credentials.email.value}")
    cachedUserCredentials match {
      case Some(result) => Future.successful(result.password == md5(credentials.password))
        //Todo refactor into sep method
      case None =>
        val signInQuery = CustomerSignInCommand.of(credentials.email.value, credentials.password)
        sphereClient
          .execute(signInQuery)
          .map {
            res => {
              Cache.set(s"userCredentials.${credentials.email.value}", UserCredentials(Email(res.getCustomer.getEmail), md5(credentials.password)))
              true
            }
          }
          .recover {
            case e: Exception =>
              false
          }
    }
  }
}

object UserService {
  def authorizeUser(userCredentials: UserCredentials): Future[Boolean] =
    Wirehelper.wired.lookupSingleOrThrow(classOf[UserService]).authorizeUser(userCredentials)
}
