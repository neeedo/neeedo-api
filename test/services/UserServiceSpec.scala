package services

import common.domain._
import common.sphere.SphereClient
import io.sphere.sdk.customers.{CustomerDraft, CustomerName, Customer, CustomerSignInResult}
import io.sphere.sdk.customers.commands._
import io.sphere.sdk.products.queries.ProductByIdFetch
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Future

class UserServiceSpec extends Specification with Mockito {

  "UserService" should {
    "createUser successfully" in {

      val username: Username = Username("Blub")
      val email: Email = Email("blub@blub.de")
      val user = User(UserId("1"), Version(1L), username, email)
      val pw: String = "pw123"
      val userDraft = UserDraft(username, email, pw)
      val customerName = CustomerName.ofFirstAndLastName(username.value, "NonEmpty")
      val customerDraft = CustomerDraft.of(customerName, email.value, pw)
      val customerCreateCommand = CustomerCreateCommand.of(customerDraft)
      val sphere = mock[SphereClient]
      val userService = new UserService(sphere)

      sphere.execute(customerCreateCommand) returns Future.successful(any[CustomerSignInResult])

      userService.createUser(userDraft)
      there was one (sphere).execute(customerCreateCommand)
    }
  }

//  "UserService.getUserByName" should {
//    "return non empty Future[Option[User]]" in {
//      this.getUserByNameSuccessfully
//    }
//
//    "return empty Future[Option[User]]" in {
//      this.failToGetUserByName
//    }
//  }
//
//  "UserService.createUser" should {
//    "return non empty Future[Option[User]]" in {
//      this.createUserSuccessfully
//    }
//
//    "return empty Future[Option[User]]" in {
//      this.failToCreateUser
//    }
//  }
//
//  "UserService.updateUser" should {
//    "return non empty Future[Option[User]]" in {
//      this.updateUserSuccessfully
//    }
//
//    "return empty Future[Option[User]]" in {
//      this.failToUpdateUser
//    }
//  }
//
//  "UserService.deleteUser" should {
//    "return non empty Future[Option[User]]" in {
//      this.deleteUserSuccessfully
//    }
//
//    "return empty Future[Option[User]]" in {
//      this.failToDeleteUser
//    }
//  }
//
//  "UserService.writeUserToSphere" should {
//    "return non empty Future[Option[User]]" in {
//      this.writeUserToSphereSuccessfully
//    }
//
//    "return empty Future[Option[User]]" in {
//      this.failToWriteUserToSphere
//    }
//  }
//
//  private def getUserByNameSuccessfully = {
//    val userDraft = UserDraft(UserCredentials(Username("name"), "pw"))
//    val userService = new UserService(mock[SphereClient])
//    val user = userService.createUser(userDraft)
//    userService.getUserByName(Username("name")) must beEqualTo(Option(user)).await
//  }
//
//  private def failToGetUserByName = {
//    val userService = new UserService(mock[SphereClient])
//    userService.getUserByName(Username("not found")) must beEqualTo(Option.empty[User]).await
//  }
//
//  private def createUserSuccessfully = {
//    val user = User(UserId("id"), Version(1L), UserCredentials(Username("name"), "pw"))
//    val userDraft = UserDraft(UserCredentials(Username("name"), "pw"))
//    val sphere = mock[SphereClient]
//    val userService = new UserService(sphere)
//
//    sphere.execute(any[CustomerCreateCommand]) returns Future.successful(Nothing)
//    userService.createUser(userDraft) must beEqualTo(Option(user))
//  }
//
//  private def failToCreateUser = {
//    val userDraft = UserDraft(UserCredentials(Username("name"), "pw"))
//    val sphere = mock[SphereClient]
//    val userService = new UserService(sphere)
//
//    sphere.execute(any[CustomerCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
//    userService.createUser(userDraft) must beEqualTo(Option.empty[User])
//  }
//
//  private def updateUserSuccessfully = {
//    val user = User(UserId("id"), Version(1L), UserCredentials(Username("updatedName"), "updatedPw"))
//    val updatedUserDraft = UserDraft(UserCredentials(Username("updatedName"), "updatedPw"))
//    val sphere = mock[SphereClient]
//    val userService = new UserService(mock[SphereClient])
//
//    sphere.execute(any[CustomerUpdateCommand]) returns Future.successful()
//    userService.updateUser(user.id, user.version, updatedUserDraft) must beEqualTo(Option(user))
//  }
//
//  private def failToUpdateUser = {
//    val user = User(UserId("id"), Version(1L), UserCredentials(Username("updatedName"), "updatedPw"))
//    val updatedUserDraft = UserDraft(UserCredentials(Username("updatedName"), "updatedPw"))
//    val sphere = mock[SphereClient]
//    val userService = new UserService(mock[SphereClient])
//
//    sphere.execute(any[CustomerUpdateCommand]) returns Future.failed(new RuntimeException("test exception"))
//    userService.updateUser(user.id, user.version, updatedUserDraft) must beEqualTo(Option.empty[User])
//  }
//
//  private def deleteUserSuccessfully = {
//    val user = User(UserId("id"), Version(1L), UserCredentials(Username("updatedName"), "updatedPw"))
//    val sphere = mock[SphereClient]
//    val userService = new UserService(mock[SphereClient])
//
//    sphere.execute(any[CustomerDeleteCommand]) returns Future.failed(new RuntimeException("test exception"))
//    userService.deleteUser(user.id, user.version) must beEqualTo(Option(user))
//  }
//
//  private def failToDeleteUser = {
//    val userService = new UserService(mock[SphereClient])
//    userService.getUserByName(Username("Not Found"))
//  }
//
//  private def writeUserToSphereSuccessfully = {
//    val userService = new UserService(mock[SphereClient])
//    userService.getUserByName(Username("Not Found"))
//  }
//
//  private def failToWriteUserToSphere = {
//    val userService = new UserService(mock[SphereClient])
//    userService.getUserByName(Username("Not Found"))
//  }
}
