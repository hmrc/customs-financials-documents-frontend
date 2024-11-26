/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package actions

import config.AppConfig
import connectors.DataStoreConnector
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.inject
import play.api.mvc.BodyParsers.Default
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import services.AuditingService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Email, Name, ~}
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PvatAuthActionSpec extends SpecBase {

  "the action" should {

    "redirect to the Government Gateway sign-in page when no authenticated user" in {
      val mockAuditingService = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = application().overrides(
        inject.bind[AuditingService].toInstance(mockAuditingService),
        inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
      ).build()

      val config = app.injector.instanceOf[AppConfig]
      val bodyParsers = app.injector.instanceOf[Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new PvatAuthAction(new FakeFailingAuthConnector(new MissingBearerToken), config, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe
          "http://localhost:9553/bas-gateway/sign-in?" +
            "continue_url=http%3A%2F%2Flocalhost%3A9876%2Fcustoms%2Fpayment-records%2Fpostponed-vat"
      }
    }

    "redirect the user to login when the user's session has expired" in {
      val mockAuditingService = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = application().overrides(
        inject.bind[AuditingService].toInstance(mockAuditingService),
        inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
      ).build()

      val config = app.injector.instanceOf[AppConfig]
      val bodyParsers = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new PvatAuthAction(new FakeFailingAuthConnector(new BearerTokenExpired), config, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(config.loginUrl)
      }
    }

    "redirect the user to login when the user has an unexpected Auth provider" in {
      val mockAuditingService = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = application().overrides(
        inject.bind[AuditingService].toInstance(mockAuditingService),
        inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
      ).build()

      val config = app.injector.instanceOf[AppConfig]
      val bodyParsers = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new PvatAuthAction(
          new FakeFailingAuthConnector(new UnsupportedAuthProvider), config, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith("/customs/documents/not-subscribed-for-cds")
      }
    }

    "redirect the user to unauthorised controller when has insufficient enrolments" in {
      val mockAuditingService = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]
      val mockAuthConnector = mock[AuthConnector]

      when(mockAuthConnector.authorise[Option[Credentials]
        ~ Option[Name] ~ Option[Email] ~ Option[AffinityGroup] ~ Option[String] ~ Enrolments](any, any)(any, any))
        .thenReturn(Future.successful(
          Some(Credentials("someProviderId", "someProviderType")) ~
            Some(Name(Some("someName"), Some("someLastName"))) ~
            Some(Email("some@email.com")) ~
            Some(AffinityGroup.Individual) ~
            Some("id") ~
            Enrolments(Set.empty)))

      val app = application().overrides(
        inject.bind[AuditingService].toInstance(mockAuditingService),
        inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
      ).build()

      val config = app.injector.instanceOf[AppConfig]
      val bodyParsers = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction = new PvatAuthAction(mockAuthConnector, config, bodyParsers, authActionHelper)
      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith("/customs/documents/not-subscribed-for-cds")
      }
    }
  }

  class Harness(authAction: PvatIdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new~(a, b)
  }
}
