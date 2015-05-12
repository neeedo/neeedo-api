package services

import common.domain._
import common.helper.Wirehelper
import common.helper.ImplicitConversions._
import common.sphere.{CustomerExceptionHandler, SphereClient}
import io.sphere.sdk.customers.queries.CustomerQuery
import io.sphere.sdk.customers.{Customer, CustomerDraft, CustomerName}
import io.sphere.sdk.customers.commands._
import io.sphere.sdk.models.Versioned
import io.sphere.sdk.queries.PagedQueryResult
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class UserService(sphereClient: SphereClient) extends CustomerExceptionHandler {

  def getUserByEmail(email: Email): Future[Option[User]] = {
    val query = CustomerQuery.of().byEmail(email.value)

    sphereClient.execute(query) map {
      case (res: PagedQueryResult[Customer]) => res.head.asScala map {
        case (customer: Customer) => User.fromCustomer(customer)
      }
    }
  }

  def createUser(userDraft: UserDraft): Future[User] = {
    val customerName = CustomerName.ofFirstAndLastName(userDraft.username.value, "NonEmpty")
    val customerDraft = CustomerDraft.of(customerName, userDraft.email.value, userDraft.password)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)

    sphereClient.execute(customerCreateCommand) map {
      res => User.fromCustomer(res.getCustomer)
    } recover {
      case ex: Exception => parseSphereCustomerException(ex)
    }
  }

  def updateUser(id: UserId, version: Version, userDraft: UserDraft): Future[User] = {
    deleteUser(id, version)
    createUser(userDraft)
  }

  def deleteUser(id: UserId, version: Version): Future[Option[User]] = {
    for {
      customer <- sphereClient.execute(CustomerDeleteCommand.of(Versioned.of(id.value, version.value)))
    } yield  {
      Cache.remove(s"userCredentials.${customer.getEmail}")
      Option(User.fromCustomer(customer))
    }
  }

  def authorizeUser(credentials: UserCredentials): Future[Boolean] = {
    val cachedUserCredentials: Option[UserCredentials] =
      Cache.getAs[UserCredentials](credentials.cacheKey)

    cachedUserCredentials match {
      case Some(result) => Future.successful(result.password == credentials.password)
      case None => sphereSignIn(credentials)
    }
  }

  def sphereSignIn(credentials: UserCredentials): Future[Boolean] = {
    val signInQuery = CustomerSignInCommand.of(credentials.email.value, credentials.password.value)

    sphereClient.execute(signInQuery).map {
      _ => {
        Cache.set(credentials.cacheKey, EncryptedUserCredentials(credentials))
        true
      }
    } recover { case e: Exception => false }
  }
}

object UserService {
  def authorizeUser(userCredentials: UserCredentials): Future[Boolean] =
    Wirehelper.wired.lookupSingleOrThrow(classOf[UserService]).authorizeUser(userCredentials)
}
