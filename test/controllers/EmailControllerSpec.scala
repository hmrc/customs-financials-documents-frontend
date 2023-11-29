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
import models.EmailUnverifiedResponse
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.inject._
import services.MetricsReporterService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.SpecBase
import play.api.test.Helpers._

import scala.concurrent.Future

class EmailControllerSpec extends SpecBase {

  "EmailController" must {
    "return unverified email" in new Setup {
      running(app) {
        val connector = app.injector.instanceOf[FinancialsApiConnector]

        val result: Future[Option[String]] = connector.isEmailUnverified(hc)
        await(result) shouldBe expectedResult
      }
    }

    "return unverified email response" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result = route(app, request).value
        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult = Some("unverifiedEmail")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    private val mockHttpClient = mock[HttpClient]
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]

    val response = EmailUnverifiedResponse(Some("unverifiedEmail"))

    when[Future[EmailUnverifiedResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
      .thenReturn(Future.successful(response))

    val app = application().overrides(
      bind[MetricsReporterService].toInstance(mockMetricsReporterService),
      bind[HttpClient].toInstance(mockHttpClient)
    ).build()
  }
}

