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

import config.AppConfig
import connectors.{DataStoreConnector, FinancialsApiConnector, SdesConnector}
import models.DutyPaymentMethod.CDS
import models.FileFormat.Pdf
import models.FileRole.PostponedVATStatement
import models.metadata.PostponedVatStatementFileMetadata
import models.{EoriHistory, PostponedVatStatementFile}
import navigation.Navigator
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.DateTimeService
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.SpecBase
import viewmodels.PostponedVatViewModel
import views.helpers.Formatters
import views.html.{postponed_import_vat, postponed_import_vat_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class PostponedVatControllerSpec extends SpecBase {

  "show" should {
    "display the PostponedVat page" in new Setup {
      config.historicStatementsEnabled = false
      val serviceUnavailableUrl: String =
        routes.ServiceUnavailableController.onPageLoad("postponed-vat").url

      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(postponedVatStatementFiles))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value

        status(result) mustBe OK

        contentAsString(result) mustBe view("testEori1",
          PostponedVatViewModel(currentStatements)(messages(app), mockDateTimeService),
          hasRequestedStatements = true,
          cdsOnly = true,
          Some("CDS"),
          Some(serviceUnavailableUrl))(request, messages(app), config).toString()

        contentAsString(result).contains(serviceUnavailableUrl)
      }
    }

    "display the PostponedVat page with no statements text when statement is not available for the " +
      "immediate previous month and accessed after 14th of the month" in new Setup {

      val currentDate: LocalDate = LocalDate.of(date.getYear, date.getMonthValue, 16)

      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime()).thenReturn(currentDate.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value

        status(result) mustBe OK

        contentAsString(result) mustBe view("testEori1",
          PostponedVatViewModel(postponedVatStatementFilesWithImmediateUnavailable)(
            messages(app), mockDateTimeService),
          hasRequestedStatements = true,
          cdsOnly = true,
          Some("CDS"),
          Some(config.historicRequestUrl(PostponedVATStatement)))(request, messages(app), config).toString()

        val doc = Jsoup.parse(contentAsString(result))
        val periodElement = Formatters.dateAsMonthAndYear(
          date.minusMonths(1))(messages(app)).replace(" ", "-").toLowerCase

        doc.getElementById(s"period-$periodElement").children().text() should include(messages(app)(
          "cf.common.not-available", Formatters.dateAsMonth(date.minusMonths(1))(messages(app))
        ))
      }
    }

    "not display the immediate previous month statement on PostponedVat page when accessed " +
      "before 15th day of the month and statement is not available" in new Setup {
      config.historicStatementsEnabled = false
      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("postponed-vat").url
      val currentDate: LocalDate = LocalDate.of(date.getYear, date.getMonthValue, 12)

      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime())
        .thenReturn(currentDate.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value
        status(result) mustBe OK

        contentAsString(result) mustBe view("testEori1",
          PostponedVatViewModel(postponedVatStatementFilesWithImmediateUnavailable)(
            messages(app), mockDateTimeService),
          hasRequestedStatements = true,
          cdsOnly = true,
          Some("CDS"),
          Some(serviceUnavailableUrl))(request, messages(app), config).toString()

        val doc = Jsoup.parse(contentAsString(result))
        val periodElement = Formatters.dateAsMonthAndYear(
          date.minusMonths(1))(messages(app)).replace(" ", "-").toLowerCase

        doc.getElementById(s"period-$periodElement") mustBe null
      }
    }

    "display historic statements Url when feature is enabled" in new Setup {
      config.historicStatementsEnabled = true
      val historicRequestUrl: String = config.historicRequestUrl(PostponedVATStatement)
      val currentDate: LocalDate = LocalDate.of(date.getYear, date.getMonthValue, 12)

      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime())
        .thenReturn(currentDate.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value
        status(result) mustBe OK

        contentAsString(result) mustBe view("testEori1",
          PostponedVatViewModel(postponedVatStatementFilesWithImmediateUnavailable)(
            messages(app), mockDateTimeService),
          hasRequestedStatements = true,
          cdsOnly = true,
          Some("CDS"),
          Some(historicRequestUrl))(request, messages(app), config).toString()
      }
    }
  }

  "statementsUnavailablePage" should {
    "display the view correctly" in {
      val app = application().build()
      val view = app.injector.instanceOf[postponed_import_vat_not_available]
      val appConfig = app.injector.instanceOf[AppConfig]
      val navigator = app.injector.instanceOf[Navigator]

      val serviceUnavailableUrl: String =
        routes.ServiceUnavailableController.onPageLoad(navigator.postponedVatNotAvailablePageId).url

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.statementsUnavailablePage().url)
        val result = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe
          view("testEori1", Some(serviceUnavailableUrl))(request, messages(app), appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector = mock[SdesConnector]
    val mockDataStoreConnector: DataStoreConnector = mock[DataStoreConnector]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val date: LocalDate = LocalDate.now()

    val postponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile("name_04", "/some-url", 111L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(7),
          monthValueOfCurrentDate(7), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 111L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(4),
          monthValueOfCurrentDate(4), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_01", "/some-url", 1300000L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(5),
          monthValueOfCurrentDate(5), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_04", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(3),
          monthValueOfCurrentDate(3), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(2),
          monthValueOfCurrentDate(2), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(1),
          monthValueOfCurrentDate(1), Pdf, PostponedVATStatement, CDS, None), "testEori1")
    )

    val postponedVatStatementFilesWithImmediateUnavailable: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile("name_04", "/some-url", 111L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(7),
          monthValueOfCurrentDate(7), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 111L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(4),
          monthValueOfCurrentDate(4), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_01", "/some-url", 1300000L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(5),
          monthValueOfCurrentDate(5), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_04", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(3),
          monthValueOfCurrentDate(3), Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(2),
          monthValueOfCurrentDate(2), Pdf, PostponedVATStatement, CDS, None), "testEori1")
    )

    val currentStatements = Seq(
      PostponedVatStatementFile("name_03", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(2024, 1, Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(2023, 12, Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_04", "/some-url", 4096L,
        PostponedVatStatementFileMetadata(2023, 11, Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_03", "/some-url", 111L,
        PostponedVatStatementFileMetadata(2023, 10, Pdf, PostponedVATStatement, CDS, None), "testEori1"),

      PostponedVatStatementFile("name_01", "/some-url", 1300000L,
        PostponedVatStatementFileMetadata(2023, 9, Pdf, PostponedVATStatement, CDS, None), "testEori1"))

    val historicPostponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile("historic_name_03", "/some-url-historic-3", 111L,
        PostponedVatStatementFileMetadata(yearValueOfCurrentDate(4),
          monthValueOfCurrentDate(4), Pdf, PostponedVATStatement, CDS, Some("1234")), "testEori2")
    )

    val app: Application = application(Seq(EoriHistory("testEori2", Some(date.minusYears(1)),
      Some(date.minusMonths(6))))).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector),
      inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector),
      inject.bind[DateTimeService].toInstance(mockDateTimeService)
    ).build()

    val view: postponed_import_vat = app.injector.instanceOf[postponed_import_vat]
    val config: AppConfig = app.injector.instanceOf[AppConfig]

    private def monthValueOfCurrentDate(monthValueToSubtract: Int): Int =
      LocalDate.now().minusMonths(monthValueToSubtract).getMonthValue

    private def yearValueOfCurrentDate(monthValueToSubtract: Int): Int =
      LocalDate.now().minusMonths(monthValueToSubtract).getYear
  }
}
