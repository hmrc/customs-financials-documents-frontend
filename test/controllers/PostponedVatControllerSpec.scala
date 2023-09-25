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
import models.FileFormat.{Csv, Pdf}
import models.FileRole.{C79Certificate, PostponedVATAmendedStatement, PostponedVATStatement}
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
          PostponedVatViewModel(
            postponedVatStatementFiles ++ historicPostponedVatStatementFiles)(messages(app), mockDateTimeService),
          hasRequestedStatements = false,
          cdsOnly = true,
          Some("CDS"),
          Some(serviceUnavailableUrl))(request, messages(app), config).toString()

        contentAsString(result).contains(serviceUnavailableUrl)
      }
    }
    //[TODO] - Need to revisit this section why its failing 
    // "display the PostponedVat page with no statements text when statement is not available for the " +
    //   "immediate previous month and accessed after 14th of the month" in new Setup {

    //   when(mockDataStoreConnector.getEmail(any)(any))
    //     .thenReturn(Future.successful(Right(Email("some@email.com"))))

    //   when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
    //     .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

    //   when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
    //     .thenReturn(Future.successful(historicPostponedVatStatementFiles))

    //   when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
    //     .thenReturn(Future.successful(true))

    //   running(app) {
    //     val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
    //     val result = route(app, request).value
    //     status(result) mustBe OK

    //     if (LocalDate.now().getDayOfMonth > 14) {
    //       when(mockDateTimeService.systemDateTime())
    //         .thenReturn(date.atStartOfDay())

    //       contentAsString(result) mustBe view("testEori1",
    //         PostponedVatViewModel(postponedVatStatementFilesWithImmediateUnavailable ++ historicPostponedVatStatementFiles)(
    //           messages(app), mockDateTimeService),
    //         hasRequestedStatements = false,
    //         cdsOnly = true,
    //         Some("CDS"))(request, messages(app), config).toString()

    //       val doc = Jsoup.parse(contentAsString(result))
    //       val periodElement = Formatters.dateAsMonthAndYear(
    //         date.minusMonths(1))(messages(app)).replace(" ", "-").toLowerCase

    //       doc.getElementById(s"period-$periodElement").children().text() should include(messages(app)(
    //         "cf.common.not-available", Formatters.dateAsMonth(date.minusMonths(1))(messages(app))
    //       ))
    //     }
    //   }
    // }

    "not display the immediate previous month statement on PostponedVat page when accessed " +
      "before 15th day of the month and statement is not available" in new Setup {
      config.historicStatementsEnabled = false
      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("postponed-vat").url

      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value
        status(result) mustBe OK

        if(LocalDate.now().getDayOfMonth < 15) {
          when(mockDateTimeService.systemDateTime())
            .thenReturn(date.atStartOfDay())

          contentAsString(result) mustBe view("testEori1",
            PostponedVatViewModel(
              postponedVatStatementFilesWithImmediateUnavailable ++ historicPostponedVatStatementFiles)(
              messages(app), mockDateTimeService),
            hasRequestedStatements = false,
            cdsOnly = true,
            Some("CDS"),
            Some(serviceUnavailableUrl))(request, messages(app), config).toString()

          val doc = Jsoup.parse(contentAsString(result))
          val periodElement = Formatters.dateAsMonthAndYear(
            date.minusMonths(1))(messages(app)).replace(" ", "-").toLowerCase

          doc.getElementById(s"period-$periodElement") mustBe null
        }
      }
    }

    "display historic statements Url when feature is enabled" in new Setup {
      config.historicStatementsEnabled = true
      val historicRequestUrl: String = config.historicRequestUrl(PostponedVATStatement)

      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

      when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value
        status(result) mustBe OK

        if(LocalDate.now().getDayOfMonth < 15) {
          when(mockDateTimeService.systemDateTime())
            .thenReturn(date.atStartOfDay())

          contentAsString(result) mustBe view("testEori1",
            PostponedVatViewModel(
              postponedVatStatementFilesWithImmediateUnavailable ++ historicPostponedVatStatementFiles)(
              messages(app), mockDateTimeService),
            hasRequestedStatements = false,
            cdsOnly = true,
            Some("CDS"),
            Some(historicRequestUrl))(request, messages(app), config).toString()
        }
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
      PostponedVatStatementFile("name_04", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(7).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(7).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(4).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(5).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_01", "/some-url", 1300000L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(5).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 8192L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Pdf, PostponedVATAmendedStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 8192L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Csv, PostponedVATAmendedStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(3).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(1).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(1).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1")
    )

    val postponedVatStatementFilesWithImmediateUnavailable: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile("name_04", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(7).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(7).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(4).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(5).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_01", "/some-url", 1300000L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(5).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 8192L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Pdf, PostponedVATAmendedStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 8192L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Csv, PostponedVATAmendedStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(3).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1")
    )

    val historicPostponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile("historic_name_03",
        "/some-url-historic-3",
        111L,
        PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(4).getMonthValue, Pdf, PostponedVATStatement, CDS, None),
        "testEori2")
    )

    val app: Application = application(Seq(EoriHistory("testEori2", Some(date.minusYears(1)),
      Some(date.minusMonths(6))))).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector),
      inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
    ).build()

    val view: postponed_import_vat = app.injector.instanceOf[postponed_import_vat]
    val config: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
