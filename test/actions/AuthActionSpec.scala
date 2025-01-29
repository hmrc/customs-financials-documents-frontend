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
import play.api.{Application, inject}
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers.*
import services.AuditingService
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import utils.Utils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class AuthActionSpec extends SpecBase with GuiceOneAppPerSuite {

  "the action" should {

    "redirect to the Government Gateway sign-in page when no authenticated user" in {
      val bodyParsers      = instanceOf[BodyParsers.Default](app)
      val authActionHelper = instanceOf[AuthActionHelper](app)

      val authAction =
        new AuthAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must startWith(appConfig.loginUrl)
    }

    "redirect the user to login when the user's session has expired" in {
      val bodyParsers      = instanceOf[BodyParsers.Default](app)
      val authActionHelper = instanceOf[AuthActionHelper](app)

      val authAction =
        new AuthAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers, authActionHelper)

      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must startWith(appConfig.loginUrl)
    }

    "redirect the user to login when the user has an unexpected Auth provider" in {
      val bodyParsers      = instanceOf[BodyParsers.Default](app)
      val authActionHelper = instanceOf[AuthActionHelper](app)

      val authAction =
        new AuthAction(
          new FakeFailingAuthConnector(new UnsupportedAuthProvider),
          appConfig,
          bodyParsers,
          authActionHelper
        )

      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must startWith("/customs/documents/not-subscribed-for-cds")
    }

    "redirect the user to unauthorised controller when has insufficient enrolments" in {
      val bodyParsers      = instanceOf[BodyParsers.Default](app)
      val authActionHelper = instanceOf[AuthActionHelper](app)

      val authAction =
        new AuthAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          appConfig,
          bodyParsers,
          authActionHelper
        )

      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must startWith("/customs/documents/not-subscribed-for-cds")
    }
  }

  override def fakeApplication(): Application = {
    val mockAuditingService    = mock[AuditingService]
    val mockDataStoreConnector = mock[DataStoreConnector]

    applicationBuilder()
      .overrides(
        inject.bind[AuditingService].toInstance(mockAuditingService),
        inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
      )
      .build()
  }

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
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
