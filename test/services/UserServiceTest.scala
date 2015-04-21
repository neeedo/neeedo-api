package services

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class UserServiceTest extends Specification with Mockito {

    "UserService.getUserByName" should {
      "return non empty Future[Option[User]]" in {
        // sphere success
      }

      "return empty Future[Option[User]]" in {
        // sphere no user found
      }

    }

    "UserService.createUser" should {
      "return non empty Future[Option[User]]" in {
        // sphere success
      }

      "return empty Future[Option[User]]" in {
        // sphere failure
      }
    }

    "UserService.updateUser" should {
      "return non empty Future[Option[User]]" in {
        // sphere success
      }

      "return empty Future[Option[User]]" in {
        // sphere failure
      }
    }

    "UserService.deleteUser" should {
      "return non empty Future[Option[User]]" in {
        // sphere success
      }

      "return empty Future[Option[User]]" in {
        // sphere throws completion exception
      }
    }

    "UserService.writeUserToSphere" should {
      "return non empty Future[Option[User]]" in {
        // sphere success
      }

      "return empty Future[Option[User]]" in {
        // sphere throws completion exception
      }
    }
}
