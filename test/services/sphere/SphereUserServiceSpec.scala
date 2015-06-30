package services.sphere


import java.util.Optional
import java.util.concurrent.CompletionException

import common.domain._
import common.exceptions.{CustomerAlreadyExists, UserNotFound}
import common.helper.UUIDHelper
import common.sphere.SphereClient
import io.sphere.sdk.client.ErrorResponseException
import io.sphere.sdk.customers.commands.{CustomerCreateCommand, CustomerDeleteCommand, CustomerSignInCommand}
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
    val password = Password("test")
    val version = Version(1L)

    val userDraft = UserDraft(username, email, password.value)
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
    val customerDraft = CustomerDraft.of(customerName, email.value, password.value)

    val customerByEmailQuery = CustomerQuery.of().byEmail(email.value)
    val customerByIdFetch = CustomerByIdFetch.of(userId.value)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)
    val customerDeleteCommand = CustomerDeleteCommand.of(Versioned.of(userId.value, version.value))
    val customerSignInCommand = CustomerSignInCommand.of(email.value, password.value)

    val duplicateFieldException = SphereError.of("DuplicateField", "")
    val customerExistsException = mock[ErrorResponseException]
    val completionException = mock[CompletionException]
    val userNotFound = mock[UserNotFound]
    val errorResponse = mock[ErrorResponseException]

  }

  "SphereUserService.getUserByEmail" should {

    "return Option[User], " +
      "if user with given email exists" in new UserServiceContext {
      sphereClient.execute(customerByEmailQuery) returns Future(pagedQueryResult)
      pagedQueryResult.head() returns Optional.of(customer)

      Await.result(sphereUserService.getUserByEmail(email),
        Duration(1, "second")) mustEqual Option(user)

      there was one (sphereClient).execute(customerByEmailQuery)
    }

    "return Option.empty[User], " +
      "if no user with given email exists" in new UserServiceContext {
      sphereClient.execute(customerByEmailQuery) returns Future(pagedQueryResult)
      pagedQueryResult.head() returns Optional.empty()

      Await.result(sphereUserService.getUserByEmail(email),
        Duration(1, "second")) mustEqual Option.empty[User]

      there was one (sphereClient).execute(customerByEmailQuery)
    }
  }

  "SphereUserService.getUserById" should {

    "return UserIdAndName, " +
      "if user with given UserId exists" in new UserServiceContext {
      sphereClient.execute(customerByIdFetch) returns Future(Optional.of(customer))

      Await.result(sphereUserService.getUserById(userId),
        Duration(1, "second")) mustEqual userIdAndName

      there was one (sphereClient).execute(customerByIdFetch)
    }

    "throw UserNotFound exception, " +
      "if no user with given UserId exists" in new UserServiceContext {
      sphereClient.execute(customerByIdFetch) returns Future(Optional.empty())

      Await.result(sphereUserService.getUserById(userId),
        Duration(1, "second")) must throwA[UserNotFound]

      there was one (sphereClient).execute(customerByIdFetch)
    }
  }

  "SphereUserService.createUser" should {

    "return User, " +
      "if sphere can create the user" in new UserServiceContext {
      sphereClient.execute(customerCreateCommand) returns Future(customerSignInResult)
      customerSignInResult.getCustomer returns customer

      Await.result(sphereUserService.createUser(userDraft),
        Duration(1, "second")) mustEqual user

      there was one (sphereClient).execute(customerCreateCommand)
    }

    "throw CustomerAlreadyExists, " +
      "if user already exists" in new UserServiceContext {
      sphereClient.execute(customerCreateCommand) returns Future.failed(customerExistsException)
      customerExistsException.getErrors returns List(duplicateFieldException).asJava

      Await.result(sphereUserService.createUser(userDraft),
        Duration(1, "second")) must throwA[CustomerAlreadyExists]

      there was one (sphereClient).execute(customerCreateCommand)
    }
  }

  "SphereUserService.deleteUser" should {

    "return User, " +
      "if user was successfully deleted" in new UserServiceContext {
      sphereClient.execute(customerDeleteCommand) returns Future(customer)

      Await.result(sphereUserService.deleteUser(userId, version),
        Duration(1, "second")) mustEqual user

      there was one (sphereClient).execute(customerDeleteCommand)
    }

    "throw UserNotFound exception, " +
      "if user was not found" in new UserServiceContext {
      sphereClient.execute(customerDeleteCommand) returns Future.failed(completionException)
      completionException.getCause returns userNotFound

      Await.result(sphereUserService.deleteUser(userId, version),
        Duration(1, "second")) must throwA[UserNotFound]

      there was one (sphereClient).execute(customerDeleteCommand)
    }
  }

  "SphereUserService.authorizeUser" should {

    "return Option[UserId], " +
      "if credentials are correct" in new UserServiceContext {
      sphereClient.execute(customerSignInCommand) returns Future(customerSignInResult)
      customerSignInResult.getCustomer returns customer

      Await.result(sphereUserService.authorizeUser(UserCredentials(email, password)),
        Duration(1, "second")) mustEqual Some(userId)

      there was one (sphereClient).execute(customerSignInCommand)
    }

    "return Option.empty, " +
      "if credentials are not correct" in new UserServiceContext {
      sphereClient.execute(customerSignInCommand) returns Future.failed(completionException)
      completionException.getCause returns errorResponse

      Await.result(sphereUserService.authorizeUser(UserCredentials(email, password)),
        Duration(1, "second")) mustEqual None

      there was one (sphereClient).execute(customerSignInCommand)
    }
  }

}
