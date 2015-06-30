package services.sphere

import java.util.concurrent.CompletionException

import common.domain._
import common.exceptions.{NetworkProblem, UserNotFound}
import common.helper.ImplicitConversions._
import common.sphere.{CustomerExceptionHandler, SphereClient}
import io.sphere.sdk.customers.commands._
import io.sphere.sdk.customers.queries.{CustomerByIdFetch, CustomerQuery}
import io.sphere.sdk.customers.{Customer, CustomerDraft, CustomerName, CustomerSignInResult}
import io.sphere.sdk.models.Versioned
import io.sphere.sdk.queries.PagedQueryResult
import play.api.Play.current
import play.api.cache.Cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class SphereUserService(sphereClient: SphereClient) extends CustomerExceptionHandler {

  def getUserByEmail(email: Email): Future[Option[User]] = {
    val query = CustomerQuery.of().byEmail(email.value)

    sphereClient.execute(query) map {
      case (res: PagedQueryResult[Customer]) => res.head.asScala map {
        case (customer: Customer) => UserFromCustomer(customer)
      }
    }
  }

  def getUserById(id: UserId): Future[UserIdAndName] = {
    val query = CustomerByIdFetch.of(id.value)

    sphereClient.execute(query) map {
      res => res.asScala match {
        case Some(customer) => UserIdAndNameFromCustomer(customer)
        case None => throw new UserNotFound(s"User with id ${id.value} does not exist")
      }
    }
  }

  def createUser(userDraft: UserDraft): Future[User] = {
    val customerName = CustomerName.ofFirstAndLastName(userDraft.username.value, "NonEmpty")
    val customerDraft = CustomerDraft.of(customerName, userDraft.email.value, userDraft.password)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)

    sphereClient.execute(customerCreateCommand) map {
      res => UserFromCustomer(res.getCustomer)
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
      Option(UserFromCustomer(customer))
    }
  }

  def authorizeUser(credentials: UserCredentials): Future[Option[UserId]] = {
    val cachedUserCredentials: Option[EncryptedUserCredentials] =
      Cache.getAs[EncryptedUserCredentials](credentials.cacheKey)

    cachedUserCredentials match {
      case Some(result) if result.md5 == PasswordHash(credentials.password) => Future.successful(Some(result.id))
      case _ => sphereSignIn(credentials)
    }
  }

  def sphereSignIn(credentials: UserCredentials): Future[Option[UserId]] = {
    val signInQuery = CustomerSignInCommand.of(credentials.email.value, credentials.password.value)

    sphereClient.execute(signInQuery).map {
      res: CustomerSignInResult => {
        val userId = UserId(res.getCustomer.getId)
        Cache.set(credentials.cacheKey, EncryptedUserCredentials(userId, credentials))
        Some(userId)
      }
    } recover {
      case e: CompletionException if e.getMessage.startsWith("java.net.ConnectException") =>
        throw new NetworkProblem("Network is currently unreachable. Please try again later.")
      case e: Exception =>
        None
    }
  }

  def UserFromCustomer(c: Customer): User =
    User(UserId(c.getId), Version(c.getVersion), Username(c.getFirstName), Email(c.getEmail))

  def UserIdAndNameFromCustomer(c: Customer): UserIdAndName =
    UserIdAndName(UserId(c.getId), Username(c.getFirstName))
}
