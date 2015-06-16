package controllers

import common.domain._
import common.exceptions.ElasticSearchIndexFailed
import common.helper.SecuredAction
import model.{MessageId, Message}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.{Result, AnyContentAsEmpty, AnyContent}
import play.api.test.{FakeHeaders, Helpers, FakeRequest, WithApplication}
import play.api.test.Helpers.defaultAwaitTimeout
import services.UserService
import services.es.EsMessageService
import test.TestData

import scala.concurrent.Future

class MessagesControllerSpec extends Specification with Mockito {

  trait MessagesControllerContext extends WithApplication {
    val userService = mock[UserService]
    val esMessageService = mock[EsMessageService]
    val securedAction = new SecuredAction(userService)
    val controller = new MessagesController(esMessageService, securedAction)

    val u1 = UserId("u1")
    val u2 = UserId("u2")

    val user1 = UserIdAndName(UserId("u1"), Username("name1"))
    val user2 = UserIdAndName(UserId("u2"), Username("name2"))

    val draft = MessageDraft(u1, u2, "")
    val message = Message(MessageId("testId"), user1, user2, "", 1L, read = false)
    val messageId = message.id

    val createMessageFakeRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/messages",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)
      .withHeaders(("Content-Type","application/json"))
      .withJsonBody(Json.toJson(draft))

    val getMessagesByUsersFakeRequest = new FakeRequest[AnyContent](
      Helpers.GET,
      "/messages",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)

    val markMessageReadFakeRequest = new FakeRequest[AnyContent](
      Helpers.PUT,
      "/messages",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)

    userService.authorizeUser(any[UserCredentials]) returns Future(Some(u1))
  }

  "MessagesController" should {

    "createMessage must return InternalServerError when service returns " +
      "ElasticSearchFailedException" in new MessagesControllerContext {
      esMessageService.createMessage(any[MessageDraft]).returns(Future.failed(new ElasticSearchIndexFailed("failed")))

      val res: Future[Result] = controller.createMessage()(createMessageFakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"failed\"}")
    }

    "createMessage must return 201 when service returns message" in new MessagesControllerContext {
      esMessageService.createMessage(any[MessageDraft]).returns(Future.successful(message))

      val res: Future[Result] = controller.createMessage()(createMessageFakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("message" -> Json.toJson(message)).toString())
    }

    "getMessagesByUsers must return 200 and the list of messages as " +
      "json for a existent pair of userIds" in new MessagesControllerContext {
      esMessageService.getMessagesByUsers(u1, u2).returns(Future.successful(List(message)))

      val res: Future[Result] = controller.getMessagesByUsers(u1, u2)(getMessagesByUsersFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("messages" -> Json.toJson(List(message))).toString())
    }

    "getMessagesByUsers must return 200 and empty list as " +
      "json for nonexistent pair of userIds" in new MessagesControllerContext {
      esMessageService.getMessagesByUsers(u1, u2).returns(Future.successful(List.empty[Message]))

      val res: Future[Result] = controller.getMessagesByUsers(u1, u2)(getMessagesByUsersFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("messages" -> List.empty[Message]).toString())
    }

    "markMessageRead must return 200 and messageId for existent Id" in new MessagesControllerContext {
      esMessageService.markMessageRead(messageId).returns(Future.successful(Option(messageId)))

      val res: Future[Result] = controller.markMessageRead(messageId)(markMessageReadFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.toJson(messageId).toString())
    }

    "markMessageRead must return 404 for nonexistent Id" in new MessagesControllerContext {
      esMessageService.markMessageRead(messageId).returns(Future.successful(Option.empty))

      val res: Future[Result] = controller.markMessageRead(messageId)(markMessageReadFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

  }

}
