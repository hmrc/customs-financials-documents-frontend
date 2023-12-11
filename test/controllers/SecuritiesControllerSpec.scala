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
import connectors.{FinancialsApiConnector, SdesConnector}
import models.FileFormat.{Csv, Pdf}
import models.FileRole.SecurityStatement
import models.metadata.SecurityStatementFileMetadata
import models.{EoriHistory, SecurityStatementFile, SecurityStatementsByPeriod, SecurityStatementsForEori}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import utils.SpecBase
import viewmodels.SecurityStatementsViewModel
import views.html.securities.{security_statements, security_statements_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class SecuritiesControllerSpec extends SpecBase {

  "showSecurityStatements" should {

    "render the page correctly on successful responses" in new Setup {
      when(mockSdesConnector.getSecurityStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.successful(Seq(securityStatementFile)))

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.showSecurityStatements.url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe
          view(SecurityStatementsViewModel(Seq(securityStatementsForEori)))(request, messages(app), appConfig).toString()
      }
    }

    "display Pdf statements only for last 6 months" in new Setup {
      when(mockSdesConnector.getSecurityStatements(any)(any))
        .thenReturn(Future.successful(Seq(securityStatementFile1, securityStatementFile2, securityStatementFile3,
          securityStatementFile4, securityStatementFile5, securityStatementFile6)))

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.showSecurityStatements.url)
        val result = route(app, request).value

        status(result) mustBe OK

        contentAsString(result) mustBe
          view(SecurityStatementsViewModel(Seq(securityStatementsPdfForEori)))(
            request, messages(app), appConfig).toString()
      }
    }

    "display Csv statements only for last 6 months" in new Setup {
      when(mockSdesConnector.getSecurityStatements(any)(any))
        .thenReturn(Future.successful(Seq(securityStatementCsvFile1, securityStatementCsvFile2,
          securityStatementCsvFile3, securityStatementCsvFile4, securityStatementCsvFile5, securityStatementCsvFile6)))

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.showSecurityStatements.url)
        val result = route(app, request).value

        status(result) mustBe OK

        contentAsString(result) mustBe
          view(SecurityStatementsViewModel(Seq(securityStatementsCsvForEori)))(
            request, messages(app), appConfig).toString()
      }
    }

    "redirect to security statements unavailable if a problem occurs" in {
      val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
      val mockSdesConnector: SdesConnector = mock[SdesConnector]

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))
      when(mockSdesConnector.getSecurityStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.failed(new RuntimeException("Something went wrong")))

      val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))

      val app: Application = application(eoriHistory).overrides(
        inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
        inject.bind[SdesConnector].toInstance(mockSdesConnector)
      ).build()

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.showSecurityStatements.url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SecuritiesController.statementsUnavailablePage().url
      }
    }
  }

  "statementsUnavailablePage" should {
    "render correctly" in {
      val app: Application = application().build()
      val appConfig = app.injector.instanceOf[AppConfig]
      val unavailableView = app.injector.instanceOf[security_statements_not_available]

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.statementsUnavailablePage().url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe unavailableView()(request, messages(app), appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector = mock[SdesConnector]
    val date: LocalDate = LocalDate.now().withDayOfMonth(28)
    val securityStatementFile: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          28,
          date.getYear,
          date.getMonthValue,
          28,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementFile1: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          10,
          date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          28,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementFile2: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(2).getYear,
          date.minusMonths(2).getMonthValue,
          15,
          date.minusMonths(2).getYear,
          date.minusMonths(2).getMonthValue,
          28,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementFile3: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(3).getYear,
          date.minusMonths(3).getMonthValue,
          12,
          date.minusMonths(3).getYear,
          date.minusMonths(3).getMonthValue,
          15,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementFile4: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(7).getYear,
          date.minusMonths(7).getMonthValue,
          10,
          date.minusMonths(7).getYear,
          date.minusMonths(7).getMonthValue,
          28,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          Some("1abcdefg2-a2b1-abcd-abcd-0123456789")))

    val securityStatementFile5: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(8).getYear,
          date.minusMonths(8).getMonthValue,
          9,
          date.minusMonths(8).getYear,
          date.minusMonths(8).getMonthValue,
          12,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementFile6: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(9).getYear,
          date.minusMonths(9).getMonthValue,
          15,
          date.minusMonths(9).getYear,
          date.minusMonths(9).getMonthValue,
          17,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))

    val statementsByPeriod: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(1), date, Seq(securityStatementFile))

    val statementsByPeriodPdfForMonth6: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile6.startDate, securityStatementFile6.endDate, Seq(securityStatementFile6))

    val statementsByPeriodPdfForMonth5: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile5.startDate, securityStatementFile5.endDate, Seq(securityStatementFile5))

    val statementsByPeriodPdfForMonth4: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile4.startDate, securityStatementFile4.endDate, Seq(securityStatementFile4))

    val statementsByPeriodPdfForMonth3: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile3.startDate, securityStatementFile3.endDate, Seq(securityStatementFile3))

    val statementsByPeriodPdfForMonth2: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile2.startDate, securityStatementFile2.endDate, Seq(securityStatementFile2))

    val statementsByPeriodPdfForMonth1: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile1.startDate, securityStatementFile1.endDate, Seq(securityStatementFile1))

    val securityStatementsForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(EoriHistory("testEori1", None, None), Seq(statementsByPeriod), Seq.empty)

    val securityStatementsPdfForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(
        EoriHistory("testEori1", None, None),
        Seq(statementsByPeriodPdfForMonth1, statementsByPeriodPdfForMonth2, statementsByPeriodPdfForMonth3),
        Seq(statementsByPeriodPdfForMonth4)
      )

    val securityStatementCsvFile1: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          10,
          date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          28,
          Csv,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementCsvFile2: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(2).getYear,
          date.minusMonths(2).getMonthValue,
          15,
          date.minusMonths(2).getYear,
          date.minusMonths(2).getMonthValue,
          28,
          Csv,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementCsvFile3: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(3).getYear,
          date.minusMonths(3).getMonthValue,
          12,
          date.minusMonths(3).getYear,
          date.minusMonths(3).getMonthValue,
          15,
          Csv,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementCsvFile4: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(7).getYear,
          date.minusMonths(7).getMonthValue,
          10,
          date.minusMonths(7).getYear,
          date.minusMonths(7).getMonthValue,
          28,
          Csv,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          Some("1abcdefg2-a2b1-abcd-abcd-0123456789")))

    val securityStatementCsvFile5: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(8).getYear,
          date.minusMonths(8).getMonthValue,
          9,
          date.minusMonths(8).getYear,
          date.minusMonths(8).getMonthValue,
          12,
          Csv,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val securityStatementCsvFile6: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(9).getYear,
          date.minusMonths(9).getMonthValue,
          15,
          date.minusMonths(9).getYear,
          date.minusMonths(9).getMonthValue,
          17,
          Csv,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val statementsByPeriodCsvForMonth6: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile6.startDate, securityStatementCsvFile6.endDate, Seq(securityStatementCsvFile6))

    val statementsByPeriodCsvForMonth5: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile5.startDate, securityStatementCsvFile5.endDate, Seq(securityStatementCsvFile5))

    val statementsByPeriodCsvForMonth4: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile4.startDate, securityStatementCsvFile4.endDate, Seq(securityStatementCsvFile4))

    val statementsByPeriodCsvForMonth3: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile3.startDate, securityStatementCsvFile3.endDate, Seq(securityStatementCsvFile3))

    val statementsByPeriodCsvForMonth2: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile2.startDate, securityStatementCsvFile2.endDate, Seq(securityStatementCsvFile2))

    val statementsByPeriodCsvForMonth1: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile1.startDate, securityStatementCsvFile1.endDate, Seq(securityStatementCsvFile1))


    val securityStatementsCsvForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(
        EoriHistory("testEori1", None, None),
        Seq(statementsByPeriodCsvForMonth1, statementsByPeriodCsvForMonth2, statementsByPeriodCsvForMonth3),
        Seq(statementsByPeriodCsvForMonth4)
      )

    when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
      .thenReturn(Future.successful(true))


    val app: Application = application(eoriHistory).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector)
    ).build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val view: security_statements = app.injector.instanceOf[security_statements]
  }
}
