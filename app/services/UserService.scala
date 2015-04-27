package services

import common.domain._
import common.helper.Wirehelper
import common.helper.ImplicitConversions._
import common.sphere.SphereClient
import io.sphere.sdk.customers.queries.CustomerQuery
import io.sphere.sdk.customers.{Customer, CustomerDraft, CustomerName}
import io.sphere.sdk.customers.commands._
import io.sphere.sdk.models.Versioned
import java.security.MessageDigest
import io.sphere.sdk.queries.{PagedQueryResult, QueryParameter}
import play.api.cache.Cache
import play.api.Play.current
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class UserService(sphereClient: SphereClient) {

  def getUserByEmail(email: String): Future[Option[User]] = {
    val query = CustomerQuery.of().withAdditionalQueryParameters( List(QueryParameter.of("email", email)).asJava )

    sphereClient.execute(query) map {
      case (res: PagedQueryResult[Customer]) => res.head.asScala map {
        case (customer: Customer) => User.fromCustomer(customer)
      }
    }
  }

  def createUser(userDraft: UserDraft): Future[Option[User]] = {
    val customerName = CustomerName.ofFirstAndLastName(userDraft.username.value, "NonEmpty")
    val customerDraft = CustomerDraft.of(customerName, userDraft.email.value, userDraft.password)
    val customerCreateCommand = CustomerCreateCommand.of(customerDraft)

    for {
      result <- sphereClient.execute(customerCreateCommand)
    } yield Option(User.fromCustomer(result.getCustomer))
  }

  def updateUser(id: UserId, version: Version, userDraft: UserDraft): Future[Option[User]] = {
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
