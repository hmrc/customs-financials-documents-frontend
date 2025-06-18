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

import com.typesafe.config.ConfigFactory
import models.*
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.*
import play.api.{Application, Configuration, inject}
import services.MetricsReporterService
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import utils.CommonTestData.{DAY_1, DAY_28, MONTH_1, MONTH_2, MONTH_3, YEAR_2018, YEAR_2019}
import utils.{SpecBase, WireMockSupportProvider}
import com.github.tomakehurst.wiremock.client.WireMock.{
  get, notFound, ok, serverError, serviceUnavailable, urlPathMatching
}
import play.api.libs.json.{JsValue, Json}
import org.scalatest.concurrent.ScalaFutures.*

import java.time.LocalDate
import scala.concurrent.Future

class DataStoreConnectorSpec extends SpecBase with WireMockSupportProvider {

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

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(
            ok(Json.toJson(eoriHistoryResponse).toString)
          )
      )

      val result: Seq[EoriHistory] = await(connector.getAllEoriHistory("someEori"))

      result mustBe expectedEoriHistory
    }

    "return the current EORI if no historic EORI's present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[Seq[EoriHistory]]](1)
        }

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(serviceUnavailable)
      )

      val result: Seq[EoriHistory] = await(connector.getAllEoriHistory("someEori"))
      result mustBe List(EoriHistory("someEori", None, None))
    }

    "return the empty seq if no historic EORI's present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[Seq[EoriHistory]]](1)
        }

      val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq())

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(ok(Json.toJson(eoriHistoryResponse).toString))
      )

      val result: Seq[EoriHistory] = await(connector.getAllEoriHistory("someEori"))
      result mustBe empty
    }

    "return emptyEoriHistory if 404 response code is received while fetching the EORI history" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[Seq[EoriHistory]]](1)
        }

      val emptyEoriHistory: Seq[EoriHistory] = Seq(EoriHistory("someEori", None, None))

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(notFound())
      )

      val result: Seq[EoriHistory] = connector.getAllEoriHistory("someEori").futureValue

      result mustBe emptyEoriHistory
      verifyEndPointUrlHit(eoriHistoryUrl)
    }

    "return emptyEoriHistory if any error response code other than 404 is received while fetching" +
      " the EORI history" in new Setup {
        when(mockMetricsReporterService.withResponseTimeLogging[Seq[EoriHistory]](any)(any)(any))
          .thenAnswer { (i: InvocationOnMock) =>
            i.getArgument[Future[Seq[EoriHistory]]](1)
          }

        val emptyEoriHistory: Seq[EoriHistory] = Seq(EoriHistory("someEori", None, None))

        wireMockServer.stubFor(
          get(urlPathMatching(eoriHistoryUrl))
            .willReturn(serverError)
        )

        val result: Seq[EoriHistory] = connector.getAllEoriHistory("someEori").futureValue

        result mustBe emptyEoriHistory
        verifyEndPointUrlHit(eoriHistoryUrl)
      }
  }

  "getEmail" should {

    "return UndeliverableEmail if the undeliverable object present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      val emailResponse: JsValue = Json.toJson(
        EmailResponse(Some("some@email.com"), None, Some(UndeliverableInformation("subject", "eventId", "groupId")))
      )

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(ok(emailResponse.toString))
      )

      val result: Either[EmailResponses, Email] = await(connector.getEmail)
      result mustBe Left(UndeliverableEmail("some@email.com"))

      verifyEndPointUrlHit(getEmailUrl)
    }

    "return Email if the undeliverable object empty" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      val emailResponse: JsValue = Json.toJson(EmailResponse(Some("some@email.com"), None, None))

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(ok(emailResponse.toString))
      )

      val result: Either[EmailResponses, Email] = await(connector.getEmail)
      result mustBe Right(Email("some@email.com"))

      verifyEndPointUrlHit(getEmailUrl)
    }

    "return Unverified if email not present" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      val emailResponse: JsValue = Json.toJson(EmailResponse(None, None, None))

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(ok(emailResponse.toString))
      )

      val result: Either[EmailResponses, Email] = await(connector.getEmail)
      result mustBe Left(UnverifiedEmail)

      verifyEndPointUrlHit(getEmailUrl)
    }

    "return Unverified if NOT_FOUND returned from the datastore" in new Setup {
      when(mockMetricsReporterService.withResponseTimeLogging[EmailResponse](any)(any)(any))
        .thenAnswer { (i: InvocationOnMock) =>
          i.getArgument[Future[EmailResponse]](1)
        }

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(notFound())
      )

      val result: Either[EmailResponses, Email] = await(connector.getEmail)
      result mustBe Left(UnverifiedEmail)

      verifyEndPointUrlHit(getEmailUrl)
    }
  }

  "verifiedEmail" should {
    "return EmailVerifiedResponse with email when the API returns a valid response" in new Setup {
      val emailResponse: EmailVerifiedResponse = EmailVerifiedResponse(Some("verified@email.com"))

      wireMockServer.stubFor(
        get(urlPathMatching(verifiedEmailUrl))
          .willReturn(ok(Json.toJson(emailResponse).toString))
      )

      val result: EmailVerifiedResponse = await(connector.verifiedEmail)
      result mustBe emailResponse

      verifyEndPointUrlHit(verifiedEmailUrl)
    }

    "return EmailVerifiedResponse with None when the API returns an error" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(verifiedEmailUrl))
          .willReturn(serverError)
      )

      val result: EmailVerifiedResponse = await(connector.verifiedEmail)
      result mustBe EmailVerifiedResponse(None)

      verifyEndPointUrlHit(verifiedEmailUrl)
    }
  }

  "retrieveUnverifiedEmail" should {
    "return EmailUnverifiedResponse with email when the API returns a valid response" in new Setup {
      val unverifiedEmailResponse: EmailUnverifiedResponse = EmailUnverifiedResponse(Some("unverified@email.com"))

      wireMockServer.stubFor(
        get(urlPathMatching(unverifiedEmailUrl))
          .willReturn(ok(Json.toJson(unverifiedEmailResponse).toString))
      )

      val result: EmailUnverifiedResponse = await(connector.retrieveUnverifiedEmail)
      result mustBe unverifiedEmailResponse

      verifyEndPointUrlHit(unverifiedEmailUrl)
    }

    "return EmailUnverifiedResponse with None when the API returns an error" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(unverifiedEmailUrl))
          .willReturn(serverError)
      )

      val result: EmailUnverifiedResponse = await(connector.retrieveUnverifiedEmail)
      result mustBe EmailUnverifiedResponse(None)

      verifyEndPointUrlHit(unverifiedEmailUrl)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |      customs-data-store {
         |      protocol = http
         |      host     = $wireMockHost
         |      port     = $wireMockPort
         |      context = "/customs-data-store"
         |    }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]

    val eoriHistoryUrl: String     = "/customs-data-store/eori/eori-history"
    val getEmailUrl: String        = "/customs-data-store/eori/verified-email"
    val verifiedEmailUrl: String   = "/customs-data-store/subscriptions/email-display"
    val unverifiedEmailUrl: String = "/customs-data-store/subscriptions/unverified-email-display"

    val app: Application = applicationBuilder()
      .configure(config)
      .overrides(
        inject.bind[MetricsReporterService].toInstance(mockMetricsReporterService)
      )
      .build()

    implicit val hc: HeaderCarrier    = HeaderCarrier()
    val connector: DataStoreConnector = instanceOf[DataStoreConnector](app)
  }
}
