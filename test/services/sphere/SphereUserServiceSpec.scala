package services.sphere


import java.util.Optional
import java.util.concurrent.CompletionException

import common.domain._
import common.exceptions.{CustomerAlreadyExists, UserNotFound}
import common.helper.UUIDHelper
import common.sphere.SphereClient
import io.sphere.sdk.client.ErrorResponseException
import io.sphere.sdk.customers.commands.{CustomerCreateCommand, CustomerDeleteCommand}
import io.sphere.sdk.customers.queries.{CustomerByIdFetch, CustomerQuery}
import io.sphere.sdk.customers.{Customer, CustomerDraft, CustomerName, CustomerSignInResult}
import io.sphere.sdk.models.{SphereError, Versioned}
import io.sphere.sdk.queries.PagedQueryResult
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.test.WithApplication

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class SphereUserServiceSpec extends Specification with Mockito {

  trait UserServiceContext extends WithApplication {
    val sphereClient = mock[SphereClient]
    val sphereUserService = new SphereUserService(sphereClient)

    val uuid = new UUIDHelper

    val userId = UserId(uuid.random)
    val email = Email("test@test.com")
    val username = Username("test")
    val password = "test"
    val version = Version(1L)

    val userDraft = UserDraft(username, email, password)
    val user = User(userId, version, username, email)
    val userIdAndName = UserIdAndName(userId, username)

    val pagedQueryResult = mock[PagedQueryResult[Customer]]
    val customerSignInResult = mock[CustomerSignInResult]

    val customer = mock[Customer]
    customer.getId returns userId.value
    customer.getVersion returns version.value
    customer.getFirstName returns username.value
    customer.getEmail returns email.value

    val customerName = CustomerName.ofFirstAndLastName(username.value, "NonEmpty")
    val customerDraft = CustomerDraft.of(customerName, email.value, password)

    val customerByEmailQuery = CustomerQuery.of().byEmail(email.value)
    val customerByIdFetch = CustomerByIdFetch.of(userId.value)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)
    val customerDeleteCommand = CustomerDeleteCommand.of(Versioned.of(userId.value, version.value))

    val duplicateFieldException = SphereError.of("DuplicateField", "")
    val customerExistsException = mock[ErrorResponseException]
    val completionException = mock[CompletionException]
    val userNotFound = mock[UserNotFound]

    completionException.getCause returns userNotFound
  }

  "SphereUserService.getUserByEmail" should {

    "return Option[User], " +
      "if user with given email exists" in new UserServiceContext {
      sphereClient.execute(any[CustomerQuery]) returns Future(pagedQueryResult)
      pagedQueryResult.head() returns Optional.of(customer)

      Await.result(sphereUserService.getUserByEmail(email),
        Duration(1, "second")) mustEqual Option(user)

      there was one (sphereClient).execute(customerByEmailQuery)
    }

    "return Option.empty[User], " +
      "if no user with given email exists" in new UserServiceContext {
      sphereClient.execute(any[CustomerQuery]) returns Future(pagedQueryResult)
      pagedQueryResult.head() returns Optional.empty()

      Await.result(sphereUserService.getUserByEmail(email),
        Duration(1, "second")) mustEqual Option.empty[User]

      there was one (sphereClient).execute(customerByEmailQuery)
    }
  }

  "SphereUserService.getUserById" should {

    "return UserIdAndName, " +
      "if user with given UserId exists" in new UserServiceContext {
      sphereClient.execute(any[CustomerByIdFetch]) returns Future(Optional.of(customer))

      Await.result(sphereUserService.getUserById(userId),
        Duration(1, "second")) mustEqual userIdAndName

      there was one (sphereClient).execute(customerByIdFetch)
    }

    "throw UserNotFound exception, " +
      "if no user with given UserId exists" in new UserServiceContext {
      sphereClient.execute(any[CustomerByIdFetch]) returns Future(Optional.empty())

      Await.result(sphereUserService.getUserById(userId),
        Duration(1, "second")) must throwA[UserNotFound]

      there was one (sphereClient).execute(customerByIdFetch)
    }
  }

  "SphereUserService.createUser" should {

    "return User" in new UserServiceContext {
      sphereClient.execute(customerCreateCommand) returns Future(customerSignInResult)
      customerSignInResult.getCustomer returns customer

      Await.result(sphereUserService.createUser(userDraft),
        Duration(1, "second")) mustEqual user

      there was one (sphereClient).execute(customerCreateCommand)
    }

    "throw CustomerAlreadyExists" in new UserServiceContext {
      sphereClient.execute(customerCreateCommand) returns Future.failed(customerExistsException)
      customerExistsException.getErrors returns List(duplicateFieldException).asJava

      Await.result(sphereUserService.createUser(userDraft),
        Duration(1, "second")) must throwA[CustomerAlreadyExists]

      there was one (sphereClient).execute(customerCreateCommand)
    }
  }

  "SphereUserService.deleteUser" should {

    "return User" in new UserServiceContext {
      sphereClient.execute(customerDeleteCommand) returns Future(customer)

      Await.result(sphereUserService.deleteUser(userId, version),
        Duration(1, "second")) mustEqual user

      there was one (sphereClient).execute(customerDeleteCommand)
    }

    "throw UserNotFound exception" in new UserServiceContext {
      sphereClient.execute(customerDeleteCommand) returns Future.failed(completionException)

      Await.result(sphereUserService.deleteUser(userId, version),
        Duration(1, "second")) must throwA[UserNotFound]

      there was one (sphereClient).execute(customerDeleteCommand)
    }
  }

}
