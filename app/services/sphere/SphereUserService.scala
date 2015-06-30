package services.sphere

import java.util.concurrent.CompletionException

import common.domain._
import common.exceptions.UserNotFound
import common.helper.ImplicitConversions._
import common.sphere.{CustomerExceptionHandler, SphereClient}
import io.sphere.sdk.client.ErrorResponseException
import io.sphere.sdk.customers.commands._
import io.sphere.sdk.customers.queries.{CustomerByIdFetch, CustomerQuery}
import io.sphere.sdk.customers.{Customer, CustomerDraft, CustomerName}
import io.sphere.sdk.models.Versioned
import io.sphere.sdk.queries.PagedQueryResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class SphereUserService(sphereClient: SphereClient) extends CustomerExceptionHandler {

  def getUserByEmail(email: Email): Future[Option[User]] = {
    val query = CustomerQuery.of().byEmail(email.value)

    sphereClient.execute(query) map {
      case (res: PagedQueryResult[Customer]) => res.head.asScala map {
        case (customer: Customer) => userFromCustomer(customer)
      }
    }
  }

  def getUserById(id: UserId): Future[UserIdAndName] = {
    val query = CustomerByIdFetch.of(id.value)

    sphereClient.execute(query) map {
      res => res.asScala match {
        case Some(customer) => userIdAndNameFromCustomer(customer)
        case None => throw new UserNotFound(s"User with id ${id.value} does not exist")
      }
    }
  }

  def createUser(userDraft: UserDraft): Future[User] = {
    val customerName = CustomerName.ofFirstAndLastName(userDraft.username.value, "NonEmpty")
    val customerDraft = CustomerDraft.of(customerName, userDraft.email.value, userDraft.password)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)

    sphereClient.execute(customerCreateCommand) map {
      res => userFromCustomer(res.getCustomer)
    } recover {
      case ex: Exception => parseSphereCustomerException(ex)
    }
  }

  def deleteUser(id: UserId, version: Version): Future[User] = {
    val deleteCommand = CustomerDeleteCommand.of(Versioned.of(id.value, version.value))

    sphereClient.execute(deleteCommand) map {
      customer => userFromCustomer(customer)
    } recover {
      case e: CompletionException =>
        throw new UserNotFound(s"No User with id=${id.value} found")
    }
  }

  def authorizeUser(credentials: UserCredentials): Future[Option[UserId]] = {
    val signInQuery = CustomerSignInCommand.of(credentials.email.value, credentials.password.value)

    sphereClient.execute(signInQuery) map {
      res => Option(UserId(res.getCustomer.getId))
    } recover {
      case e: CompletionException if e.getCause.isInstanceOf[ErrorResponseException] => None
    }
  }

  private def userFromCustomer(c: Customer): User =
    User(UserId(c.getId), Version(c.getVersion), Username(c.getFirstName), Email(c.getEmail))

  private def userIdAndNameFromCustomer(c: Customer): UserIdAndName =
    UserIdAndName(UserId(c.getId), Username(c.getFirstName))
}
