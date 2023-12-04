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

package connectors

import models.{EmailUnverifiedResponse, EmailVerifiedResponse, FileRole}
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.MetricsReporterService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.SpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialsApiConnectorSpec extends SpecBase {

  "delete notifications should return a boolean based on the result" in new Setup {
    when(mockMetricsReporterService.withResponseTimeLogging[HttpResponse](any)(any)(any))
      .thenAnswer((i: InvocationOnMock) => {
        i.getArgument[Future[HttpResponse]](1)
      })

    when[Future[HttpResponse]](mockHttpClient.DELETE(any, any)(any, any, any))
      .thenReturn(Future.successful(HttpResponse(200, "")))

    running(app) {
      val result = await(connector.deleteNotification("someEori", FileRole.C79Certificate))
      result mustBe true
    }
  }

  "return unverified email" in new Setup {
    when[Future[EmailUnverifiedResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
      .thenReturn(Future.successful(EmailUnverifiedResponse(Some("unverified@email.com"))))

    running(app) {
      val result = await(connector.isEmailUnverified(hc))
      result mustBe Some("unverified@email.com")
    }
  }

  "verifiedEmail" should {
    "return correct email address" in new Setup {
      when[Future[EmailVerifiedResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(EmailVerifiedResponse(Some(email))))

      running(app) {
        val result: Future[EmailVerifiedResponse] = connector.verifiedEmail

        result.map {
          res => res mustBe sampleEmailVerifiedResponse
        }
      }
    }

    "return None when there is no email address returned" in new Setup {
      when[Future[EmailVerifiedResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(EmailVerifiedResponse(None)))

      running(app) {
        val result = connector.verifiedEmail

        result.map {
          res => res.verifiedEmail mustBe empty
        }
      }
    }
  }

  trait Setup {
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockHttpClient: HttpClient = mock[HttpClient]

    val app: Application = application().overrides(
      inject.bind[MetricsReporterService].toInstance(mockMetricsReporterService),
      inject.bind[HttpClient].toInstance(mockHttpClient)
    ).build()

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val connector: FinancialsApiConnector = app.injector.instanceOf[FinancialsApiConnector]

    val email = "test@test.com"
    val sampleEmailVerifiedResponse: EmailVerifiedResponse = EmailVerifiedResponse(Some(email))
  }
}
