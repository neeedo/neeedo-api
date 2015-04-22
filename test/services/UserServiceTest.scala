package services

import common.domain._
import common.sphere.SphereClient
import org.specs2.matcher.MatchResult
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Future

class UserServiceTest extends Specification with Mockito {

  "UserService.getUserByName" should {
    "return non empty Future[Option[User]]" in {
      this.getUserByNameSuccessfully
    }

    "return empty Future[Option[User]]" in {
      this.failToGetUserByName
    }
  }

  "UserService.createUser" should {
    "return non empty Future[Option[User]]" in {
      this.createUserSuccessfully
    }

    "return empty Future[Option[User]]" in {
      this.failToCreateUser
    }
  }

  "UserService.updateUser" should {
    "return non empty Future[Option[User]]" in {
      this.updateUserSuccessfully
    }

    "return empty Future[Option[User]]" in {
      this.failToUpdateUser
    }
  }

  "UserService.deleteUser" should {
    "return non empty Future[Option[User]]" in {
      this.deleteUserSuccessfully
    }

    "return empty Future[Option[User]]" in {
      this.failToDeleteUser
    }
  }

  "UserService.writeUserToSphere" should {
    "return non empty Future[Option[User]]" in {
      this.writeUserToSphereSuccessfully
    }

    "return empty Future[Option[User]]" in {
      this.failToWriteUserToSphere
    }
  }

  private def getUserByNameSuccessfully = {
    val userDraft = UserDraft(Username("name"))
    val userService = new UserService(mock[SphereClient])
    val user = userService.createUser(userDraft)
    userService.getUserByName(Username("name")) must beEqualTo(Option(user)).await
  }

  private def failToGetUserByName = {
    val userService = new UserService(mock[SphereClient])
    userService.getUserByName(Username("not found")) must beEqualTo(Option.empty[User]).await
  }

  private def createUserSuccessfully = {
    val userDraft = UserDraft(Username("name"))
    val userService = new UserService(mock[SphereClient])
    val user = ???
    userService.createUser(userDraft) must beEqualTo(Option(user))
  }

  private def failToCreateUser = {
    val userDraft = UserDraft(Username("name"))
    val userService = new UserService(mock[SphereClient])
    userService.createUser(userDraft)
    userService.createUser(userDraft) must beEqualTo(Option.empty[User])
  }

  private def updateUserSuccessfully = {
    val userService = new UserService(mock[SphereClient])
    userService.updateUser(Username("Not Found"))
  }

  private def failToUpdateUser = {
    val userService = new UserService(mock[SphereClient])
    userService.getUserByName(Username("Not Found"))
  }

  private def deleteUserSuccessfully = {
    val userService = new UserService(mock[SphereClient])
    userService.getUserByName(Username("Not Found"))
  }

  private def failToDeleteUser = {
    val userService = new UserService(mock[SphereClient])
    userService.getUserByName(Username("Not Found"))
  }

  private def writeUserToSphereSuccessfully = {
    val userService = new UserService(mock[SphereClient])
    userService.getUserByName(Username("Not Found"))
  }

  private def failToWriteUserToSphere = {
    val userService = new UserService(mock[SphereClient])
    userService.getUserByName(Username("Not Found"))
  }

}
