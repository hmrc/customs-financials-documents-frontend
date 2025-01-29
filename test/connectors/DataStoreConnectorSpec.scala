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

import models.*
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.MetricsReporterService
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.CommonTestData.{DAY_1, DAY_28, MONTH_1, MONTH_2, MONTH_3, YEAR_2018, YEAR_2019}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class DataStoreConnectorSpec extends SpecBase {

  "getAllEoriHistory" should {

    "return EoriHistory if any historic EORI's present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[Seq[EoriHistory]]](1)
        }

      val historyDate1: LocalDate = LocalDate.of(YEAR_2019, MONTH_3, DAY_1)
      val historyDate2: LocalDate = LocalDate.of(YEAR_2018, MONTH_1, DAY_1)
      val historyDate3: LocalDate = LocalDate.of(YEAR_2019, MONTH_2, DAY_28)

      val expectedEoriHistory: Seq[EoriHistory] = List(
        EoriHistory("GB11111", Some(historyDate1), None),
        EoriHistory("GB22222", Some(historyDate2), Some(historyDate3))
      )

      val eoriHistory1: EoriHistory = EoriHistory("GB11111", validFrom = Some(historyDate1), None)
      val eoriHistory2: EoriHistory =
        EoriHistory("GB22222", validFrom = Some(historyDate2), validUntil = Some(historyDate3))

      val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq(eoriHistory1, eoriHistory2))

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(eoriHistoryResponse))

      running(app) {
        val result = await(connector.getAllEoriHistory("someEori"))
        result mustBe expectedEoriHistory
      }
    }

    "return the current EORI if no historic EORI's present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[Seq[EoriHistory]]](1)
        }

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any))
        .thenReturn(Future.failed(new RuntimeException("No history found")))

      running(app) {
        val result = await(connector.getAllEoriHistory("someEori"))
        result mustBe List(EoriHistory("someEori", None, None))
      }
    }
  }

  "getEmail" should {
    "return UndeliverableEmail if the undeliverable object present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(
        Future.successful(
          EmailResponse(Some("some@email.com"), None, Some(UndeliverableInformation("subject", "eventId", "groupId")))
        )
      )

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Left(UndeliverableEmail("some@email.com"))
      }
    }

    "return Email if the undeliverable object empty" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any))
        .thenReturn(Future.successful(EmailResponse(Some("some@email.com"), None, None)))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Right(Email("some@email.com"))
      }
    }

    "return Unverified if email not present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(EmailResponse(None, None, None)))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Left(UnverifiedEmail)
      }
    }

    "return Unverified if NOT_FOUND returned from the datastore" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("NoData", NOT_FOUND, NOT_FOUND)))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Left(UnverifiedEmail)
      }
    }
  }

  "verifiedEmail" should {
    "return EmailVerifiedResponse with email when the API returns a valid response" in new Setup {
      val emailResponse = EmailVerifiedResponse(Some("verified@email.com"))

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(emailResponse))

      running(app) {
        val result = await(connector.verifiedEmail)
        result mustBe emailResponse
      }
    }

    "return EmailVerifiedResponse with None when the API returns an error" in new Setup {

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any))
        .thenReturn(Future.failed(new RuntimeException("API call failed")))

      running(app) {
        val result = await(connector.verifiedEmail)
        result mustBe EmailVerifiedResponse(None)
      }
    }
  }

  "retrieveUnverifiedEmail" should {
    "return EmailUnverifiedResponse with email when the API returns a valid response" in new Setup {
      val unverifiedEmailResponse = EmailUnverifiedResponse(Some("unverified@email.com"))

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(Future.successful(unverifiedEmailResponse))

      running(app) {
        val result = await(connector.retrieveUnverifiedEmail)
        result mustBe unverifiedEmailResponse
      }
    }

    "return EmailUnverifiedResponse with None when the API returns an error" in new Setup {

      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any))
        .thenReturn(Future.failed(new RuntimeException("API call failed")))

      running(app) {
        val result = await(connector.retrieveUnverifiedEmail)
        result mustBe EmailUnverifiedResponse(None)
      }
    }
  }

  trait Setup {
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockHttpClient: HttpClientV2                       = mock[HttpClientV2]
    val requestBuilder: RequestBuilder                     = mock[RequestBuilder]

    val app: Application = applicationBuilder()
      .overrides(
        inject.bind[MetricsReporterService].toInstance(mockMetricsReporterService),
        inject.bind[HttpClientV2].toInstance(mockHttpClient)
      )
      .build()

    implicit val hc: HeaderCarrier    = HeaderCarrier()
    val connector: DataStoreConnector = instanceOf[DataStoreConnector](app)
  }
}
