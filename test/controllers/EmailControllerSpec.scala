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

import connectors.DataStoreConnector
import models.{EmailUnverifiedResponse, EmailVerifiedResponse}

import play.api.inject._
import play.api.test.Helpers._
import services.MetricsReporterService
import uk.gov.hmrc.http.HeaderCarrier
import utils.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import scala.concurrent.Future
import play.api.Application

class EmailControllerSpec extends SpecBase {

  "showUnverified" must {
    "return unverified email response" in new Setup {

      when(mockConnector.retrieveUnverifiedEmail(any)).thenReturn(Future.successful(emailUnverifiedResponse))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)

        val result = route(app, request).value
        status(result) shouldBe OK
      }
    }

    "display verify your email page when exception occurs while connector making the API call" in new Setup {

      when(mockConnector.retrieveUnverifiedEmail(any))
        .thenReturn(Future.successful(emailUnverifiedResponseWithNoEmailId))

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
        val result  = route(app, request).value

        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult: Option[String] = Some("unverifiedEmail")
    implicit val hc: HeaderCarrier     = HeaderCarrier()

    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockConnector: DataStoreConnector                  = mock[DataStoreConnector]

    val emailUnverifiedResponse: EmailUnverifiedResponse              = EmailUnverifiedResponse(Some("unverifiedEmail"))
    val emailUnverifiedResponseWithNoEmailId: EmailUnverifiedResponse = EmailUnverifiedResponse(None)
    val emailVerifiedResponse: EmailVerifiedResponse                  = EmailVerifiedResponse(Some("test@test.com"))

    val app: Application = applicationBuilder()
      .overrides(
        bind[MetricsReporterService].toInstance(mockMetricsReporterService),
        bind[DataStoreConnector].toInstance(mockConnector)
      )
      .build()
  }
}
