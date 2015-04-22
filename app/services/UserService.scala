package services

import java.security.MessageDigest
import java.util.Optional

import common.domain._
import common.helper.Wirehelper
import common.sphere.SphereClient
import io.sphere.sdk.customers.commands.CustomerSignInCommand
import scala.concurrent.Future
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

class UserService(sphereClient: SphereClient) {
  def getUserByMail(mail: String): Future[Option[User]] = {
    Future.successful(None)
  }
  def createUser(): Future[Option[User]] = {
    Future.successful(None)
  }
  def updateUser(id: UserId, version: Version): Future[Option[User]] = {
    // invalidate cache for user
    Future.successful(None)
  }
  def deleteUser(id: UserId, version: Version): Future[Option[User]] = {
    // invalidate cache for user
    Future.successful(None)
  }

  def writeUserToSphere(draft: UserDraft): Future[Option[User]] = Future.successful(None)
  def authorizeUser(userCredentials: UserCredentials): Future[Boolean] = {
    def md5(s: String): String = new String(MessageDigest.getInstance("MD5").digest(s.getBytes))

    val cachedUser: Option[User] = Cache.getAs[User](s"user.${userCredentials.mail.value}")
    cachedUser match {
      case Some(user) => Future.successful(user.password == md5(userCredentials.pw))
      case None =>
        val signInQuery = CustomerSignInCommand.of(userCredentials.mail.value, userCredentials.pw, Optional.empty())
        sphereClient
          .execute(signInQuery)
          .map {
            res => {
              Cache.set(s"user.${userCredentials.mail.value}", User(res.getCustomer, md5(userCredentials.pw)))
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