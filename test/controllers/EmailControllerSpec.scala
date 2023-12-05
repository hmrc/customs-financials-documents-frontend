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

package controllers

import connectors.FinancialsApiConnector
import models.{EmailUnverifiedResponse, EmailVerifiedResponse}
import play.api.Application
import play.api.inject._
import play.api.test.Helpers._
import services.MetricsReporterService
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase

import scala.concurrent.Future

class EmailControllerSpec extends SpecBase {

  "showUnverified" must {
    "return unverified email" in new Setup {
      when(mockConnector.isEmailUnverified(any)).thenReturn(Future.successful(Some("unverifiedEmail")))

      running(app) {
        val connector = app.injector.instanceOf[FinancialsApiConnector]

        val result: Future[Option[String]] = connector.isEmailUnverified(hc)
        await(result) shouldBe expectedResult
      }
    }

    "return unverified email response" in new Setup {
      when(mockConnector.isEmailUnverified(any)).thenReturn(Future.successful(Some("test@test.com")))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result = route(app, request).value
        status(result) shouldBe OK
      }
    }
  }

  "showUndeliverable" must {
    "display undeliverableEmail page" in new Setup {

      when(mockConnector.verifiedEmail(any)).thenReturn(Future.successful(emailVerifiedResponse))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
        val result = route(app, request).value

        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult: Option[String] = Some("unverifiedEmail")
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockConnector: FinancialsApiConnector = mock[FinancialsApiConnector]

    val response: EmailUnverifiedResponse = EmailUnverifiedResponse(Some("unverifiedEmail"))
    val emailVerifiedResponse: EmailVerifiedResponse = EmailVerifiedResponse(Some("test@test.com"))

    val app: Application = application().overrides(
      bind[MetricsReporterService].toInstance(mockMetricsReporterService),
      bind[FinancialsApiConnector].toInstance(mockConnector)
    ).build()
  }
}
