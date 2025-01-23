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

import com.google.inject.Inject
import config.AppConfig
import connectors.DataStoreConnector

import play.api.inject
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import services.AuditingService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import utils.Utils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

import org.scalatest.matchers.must.Matchers.{must, mustBe}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
  }

  "the action" should {

    "redirect to the Government Gateway sign-in page when no authenticated user" in {
      val mockAuditingService    = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = applicationBuilder()
        .overrides(
          inject.bind[AuditingService].toInstance(mockAuditingService),
          inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
        )
        .build()

      val bodyParsers      = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new AuthAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(appConfig.loginUrl)
      }
    }

    "redirect the user to login when the user's session has expired" in {
      val mockAuditingService    = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = applicationBuilder()
        .overrides(
          inject.bind[AuditingService].toInstance(mockAuditingService),
          inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
        )
        .build()

      val bodyParsers      = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new AuthAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(appConfig.loginUrl)
      }
    }

    "redirect the user to login when the user has an unexpected Auth provider" in {
      val mockAuditingService    = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = applicationBuilder()
        .overrides(
          inject.bind[AuditingService].toInstance(mockAuditingService),
          inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
        )
        .build()

      val bodyParsers      = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new AuthAction(
          new FakeFailingAuthConnector(new UnsupportedAuthProvider),
          appConfig,
          bodyParsers,
          authActionHelper
        )

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith("/customs/documents/not-subscribed-for-cds")
      }
    }

    "redirect the user to unauthorised controller when has insufficient enrolments" in {
      val mockAuditingService    = mock[AuditingService]
      val mockDataStoreConnector = mock[DataStoreConnector]

      val app = applicationBuilder()
        .overrides(
          inject.bind[AuditingService].toInstance(mockAuditingService),
          inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
        )
        .build()

      val bodyParsers      = app.injector.instanceOf[BodyParsers.Default]
      val authActionHelper = app.injector.instanceOf[AuthActionHelper]

      val authAction =
        new AuthAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          appConfig,
          bodyParsers,
          authActionHelper
        )

      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith("/customs/documents/not-subscribed-for-cds")
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = emptyString

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
