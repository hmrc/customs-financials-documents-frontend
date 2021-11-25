/*
 * Copyright 2021 HM Revenue & Customs
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

import models._
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.MetricsReporterService
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.Future

class DataStoreConnectorSpec extends SpecBase {

  "getAllEoriHistory" should {
    "return EoriHistory if any historic EORI's present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer((i: InvocationOnMock) => {
          i.getArgument[Future[Seq[EoriHistory]]](1)
        })

      val historyDate1: LocalDate = LocalDate.of(2019, 3, 1)
      val historyDate2: LocalDate = LocalDate.of(2018, 1, 1)
      val historyDate3: LocalDate = LocalDate.of(2019, 2, 28)

      val expectedEoriHistory: Seq[EoriHistory] = List(
        EoriHistory("GB11111", Some(historyDate1), None),
        EoriHistory("GB22222", Some(historyDate2), Some(historyDate3))
      )

      val eoriHistory1: EoriHistory = EoriHistory("GB11111", validFrom = Some(historyDate1), None)
      val eoriHistory2: EoriHistory = EoriHistory("GB22222", validFrom = Some(historyDate2), validUntil = Some(historyDate3))
      val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq(eoriHistory1, eoriHistory2))

      when[Future[EoriHistoryResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(eoriHistoryResponse))

      running(app) {
        val result = await(connector.getAllEoriHistory("someEori"))
        result mustBe expectedEoriHistory
      }
    }

    "return the current EORI if no historic EORI's present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer((i: InvocationOnMock) => {
          i.getArgument[Future[Seq[EoriHistory]]](1)
        })


      when[Future[EoriHistoryResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
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
        .thenAnswer((i: InvocationOnMock) => {
          i.getArgument[Future[EmailResponse]](1)
        })

      when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(EmailResponse(Some("some@email.com"), None, Some(UndeliverableInformation("subject", "eventId", "groupId")))))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Left(UndeliverableEmail("some@email.com"))
      }
    }

    "return Email if the undeliverable object empty" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer((i: InvocationOnMock) => {
          i.getArgument[Future[EmailResponse]](1)
        })

      when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(EmailResponse(Some("some@email.com"), None, None)))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Right(Email("some@email.com"))
      }
    }

    "return Unverified if email not present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer((i: InvocationOnMock) => {
          i.getArgument[Future[EmailResponse]](1)
        })

      when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(EmailResponse(None, None, None)))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Left(UnverifiedEmail)
      }
    }

    "return Unverified if NOT_FOUND returned from the datastore" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer((i: InvocationOnMock) => {
          i.getArgument[Future[EmailResponse]](1)
        })

      when[Future[EmailResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.failed(UpstreamErrorResponse("NoData", 404, 404)))

      running(app) {
        val result = await(connector.getEmail("someEori"))
        result mustBe Left(UnverifiedEmail)
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
    val connector: DataStoreConnector = app.injector.instanceOf[DataStoreConnector]
  }
}
