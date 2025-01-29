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
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import utils.CommonTestData.{
  CHECK_SUM_000000, DAY_10, DAY_12, DAY_15, DAY_17, DAY_28, DAY_9, DOWNLOAD_URL_00, EIGHT_MONTHS, EORI_NUMBER,
  NINE_MONTHS, ONE_MONTH, SEVEN_MONTHS, SIZE_500L, SIZE_99L, STAT_FILE_NAME_00, TEN_MONTHS, THREE_MONTHS, TWO_MONTHS
}
import utils.SpecBase
import viewmodels.SecurityStatementsViewModel
import views.html.securities.{security_statements, security_statements_not_available}
import org.mockito.ArgumentMatchers.{eq => eqTo}
import java.time.LocalDate
import scala.concurrent.Future

class SecuritiesControllerSpec extends SpecBase {

  "showSecurityStatements" should {

    "render the page correctly on successful responses" in new Setup {
      when(mockSdesConnector.getSecurityStatements(eqTo(EORI_NUMBER))(any))
        .thenReturn(Future.successful(Seq(securityStatementFile)))

      running(app) {
        status(result) mustBe OK
        contentAsString(result) mustBe view(SecurityStatementsViewModel(Seq(securityStatementsForEori))).toString()
      }
    }

    "display Pdf statements only for last 6 months" in new Setup {
      when(mockSdesConnector.getSecurityStatements(any)(any))
        .thenReturn(
          Future.successful(
            Seq(
              securityStatementFile1,
              securityStatementFile2,
              securityStatementFile3,
              securityStatementFile4,
              securityStatementFile5,
              securityStatementFile6
            )
          )
        )

      running(app) {
        status(result) mustBe OK
        contentAsString(result) mustBe view(SecurityStatementsViewModel(Seq(securityStatementsPdfForEori))).toString()
      }
    }

    "display Csv statements only for last 6 months" in new Setup {
      when(mockSdesConnector.getSecurityStatements(any)(any))
        .thenReturn(
          Future.successful(
            Seq(
              securityStatementCsvFile1,
              securityStatementCsvFile2,
              securityStatementCsvFile3,
              securityStatementCsvFile4,
              securityStatementCsvFile5,
              securityStatementCsvFile6
            )
          )
        )

      running(app) {
        status(result) mustBe OK
        contentAsString(result) mustBe view(SecurityStatementsViewModel(Seq(securityStatementsCsvForEori))).toString()
      }
    }

    "display requested statements link when historic statements are available" in new Setup {
      when(mockSdesConnector.getSecurityStatements(any)(any))
        .thenReturn(
          Future.successful(
            Seq(
              securityStatementFile1,
              securityStatementFile2,
              securityStatementFile3,
              securityStatementFile4,
              securityStatementFile5,
              securityStatementFile6,
              securityStatementFile7,
              securityStatementFile8
            )
          )
        )

      running(app) {
        status(result) mustBe OK
        contentAsString(result) mustBe view(SecurityStatementsViewModel(Seq(securityStatementsPdfForEori))).toString()

        val doc = Jsoup.parse(contentAsString(result))
        Option(doc.getElementById("request-statement-link")) should not be empty
      }
    }

    "redirect to security statements unavailable if a problem occurs" in new Setup {
      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))
      when(mockSdesConnector.getSecurityStatements(eqTo(EORI_NUMBER))(any))
        .thenReturn(Future.failed(new RuntimeException("Something went wrong")))

      running(app) {
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SecuritiesController.statementsUnavailablePage().url
      }
    }
  }

  "statementsUnavailablePage" should {

    "render correctly" in {
      val unavailableView = instanceOf[security_statements_not_available](application)

      running(application) {
        val request = fakeRequest(GET, routes.SecuritiesController.statementsUnavailablePage().url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe unavailableView()(request, messages, appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector                   = mock[SdesConnector]

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory(EORI_NUMBER, None, None))

    val app: Application = applicationBuilder(eoriHistory)
      .overrides(
        inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
        inject.bind[SdesConnector].toInstance(mockSdesConnector)
      )
      .build()

    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.SecuritiesController.showSecurityStatements().url)

    val result: Future[Result]    = route(app, request).value
    val view: security_statements = instanceOf[security_statements](app)

    val date: LocalDate               = LocalDate.now().withDayOfMonth(DAY_28)
    val someRequestId: Option[String] = Some("statement-request-id")

    when(mockFinancialsApiConnector.deleteNotification(any, any)(any)).thenReturn(Future.successful(true))

    val securityStatementFile: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(ONE_MONTH).getYear,
          date.minusMonths(ONE_MONTH).getMonthValue,
          DAY_28,
          date.getYear,
          date.getMonthValue,
          DAY_28,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile1: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(ONE_MONTH).getYear,
          date.minusMonths(ONE_MONTH).getMonthValue,
          DAY_10,
          date.minusMonths(ONE_MONTH).getYear,
          date.minusMonths(ONE_MONTH).getMonthValue,
          DAY_28,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile2: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(TWO_MONTHS).getYear,
          date.minusMonths(TWO_MONTHS).getMonthValue,
          DAY_15,
          date.minusMonths(TWO_MONTHS).getYear,
          date.minusMonths(TWO_MONTHS).getMonthValue,
          DAY_28,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile3: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(THREE_MONTHS).getYear,
          date.minusMonths(THREE_MONTHS).getMonthValue,
          DAY_12,
          date.minusMonths(THREE_MONTHS).getYear,
          date.minusMonths(THREE_MONTHS).getMonthValue,
          DAY_15,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile4: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(SEVEN_MONTHS).getYear,
          date.minusMonths(SEVEN_MONTHS).getMonthValue,
          DAY_10,
          date.minusMonths(SEVEN_MONTHS).getYear,
          date.minusMonths(SEVEN_MONTHS).getMonthValue,
          DAY_28,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          Some("1abcdefg2-a2b1-abcd-abcd-0123456789")
        )
      )

    val securityStatementFile5: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(EIGHT_MONTHS).getYear,
          date.minusMonths(EIGHT_MONTHS).getMonthValue,
          DAY_9,
          date.minusMonths(EIGHT_MONTHS).getYear,
          date.minusMonths(EIGHT_MONTHS).getMonthValue,
          DAY_12,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile6: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(NINE_MONTHS).getYear,
          date.minusMonths(NINE_MONTHS).getMonthValue,
          DAY_15,
          date.minusMonths(NINE_MONTHS).getYear,
          date.minusMonths(NINE_MONTHS).getMonthValue,
          DAY_17,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile7: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(TEN_MONTHS).getYear,
          date.minusMonths(TEN_MONTHS).getMonthValue,
          DAY_15,
          date.minusMonths(TEN_MONTHS).getYear,
          date.minusMonths(TEN_MONTHS).getMonthValue,
          DAY_17,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFile8: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(TEN_MONTHS).getYear,
          date.minusMonths(TEN_MONTHS).getMonthValue,
          DAY_15,
          date.minusMonths(TEN_MONTHS).getYear,
          date.minusMonths(TEN_MONTHS).getMonthValue,
          DAY_17,
          Pdf,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          someRequestId
        )
      )

    val statementsByPeriod: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(ONE_MONTH), date, Seq(securityStatementFile))

    val statementsByPeriodPdfForMonth7: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile7.startDate,
        securityStatementFile7.endDate,
        Seq(securityStatementFile7, securityStatementFile8)
      )

    val statementsByPeriodPdfForMonth4: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile4.startDate,
        securityStatementFile4.endDate,
        Seq(securityStatementFile4)
      )

    val statementsByPeriodPdfForMonth3: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile3.startDate,
        securityStatementFile3.endDate,
        Seq(securityStatementFile3)
      )

    val statementsByPeriodPdfForMonth2: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile2.startDate,
        securityStatementFile2.endDate,
        Seq(securityStatementFile2)
      )

    val statementsByPeriodPdfForMonth1: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementFile1.startDate,
        securityStatementFile1.endDate,
        Seq(securityStatementFile1)
      )

    val securityStatementsForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(EoriHistory(EORI_NUMBER, None, None), Seq(statementsByPeriod), Seq.empty)

    val securityStatementsPdfForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(
        EoriHistory(EORI_NUMBER, None, None),
        Seq(statementsByPeriodPdfForMonth1, statementsByPeriodPdfForMonth2, statementsByPeriodPdfForMonth3),
        Seq(statementsByPeriodPdfForMonth4, statementsByPeriodPdfForMonth7)
      )

    val securityStatementCsvFile1: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(ONE_MONTH).getYear,
          date.minusMonths(ONE_MONTH).getMonthValue,
          DAY_10,
          date.minusMonths(ONE_MONTH).getYear,
          date.minusMonths(ONE_MONTH).getMonthValue,
          DAY_28,
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementCsvFile2: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(TWO_MONTHS).getYear,
          date.minusMonths(TWO_MONTHS).getMonthValue,
          DAY_15,
          date.minusMonths(TWO_MONTHS).getYear,
          date.minusMonths(TWO_MONTHS).getMonthValue,
          DAY_28,
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementCsvFile3: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(THREE_MONTHS).getYear,
          date.minusMonths(THREE_MONTHS).getMonthValue,
          DAY_12,
          date.minusMonths(THREE_MONTHS).getYear,
          date.minusMonths(THREE_MONTHS).getMonthValue,
          DAY_15,
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementCsvFile4: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(SEVEN_MONTHS).getYear,
          date.minusMonths(SEVEN_MONTHS).getMonthValue,
          DAY_10,
          date.minusMonths(SEVEN_MONTHS).getYear,
          date.minusMonths(SEVEN_MONTHS).getMonthValue,
          DAY_28,
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          Some("1abcdefg2-a2b1-abcd-abcd-0123456789")
        )
      )

    val securityStatementCsvFile5: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(EIGHT_MONTHS).getYear,
          date.minusMonths(EIGHT_MONTHS).getMonthValue,
          DAY_9,
          date.minusMonths(EIGHT_MONTHS).getYear,
          date.minusMonths(EIGHT_MONTHS).getMonthValue,
          DAY_12,
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementCsvFile6: SecurityStatementFile =
      SecurityStatementFile(
        STAT_FILE_NAME_00,
        DOWNLOAD_URL_00,
        SIZE_99L,
        SecurityStatementFileMetadata(
          date.minusMonths(NINE_MONTHS).getYear,
          date.minusMonths(NINE_MONTHS).getMonthValue,
          DAY_15,
          date.minusMonths(NINE_MONTHS).getYear,
          date.minusMonths(NINE_MONTHS).getMonthValue,
          DAY_17,
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val statementsByPeriodCsvForMonth4: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile4.startDate,
        securityStatementCsvFile4.endDate,
        Seq(securityStatementCsvFile4)
      )

    val statementsByPeriodCsvForMonth3: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile3.startDate,
        securityStatementCsvFile3.endDate,
        Seq(securityStatementCsvFile3)
      )

    val statementsByPeriodCsvForMonth2: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile2.startDate,
        securityStatementCsvFile2.endDate,
        Seq(securityStatementCsvFile2)
      )

    val statementsByPeriodCsvForMonth1: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(
        securityStatementCsvFile1.startDate,
        securityStatementCsvFile1.endDate,
        Seq(securityStatementCsvFile1)
      )

    val securityStatementsCsvForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(
        EoriHistory(EORI_NUMBER, None, None),
        Seq(statementsByPeriodCsvForMonth1, statementsByPeriodCsvForMonth2, statementsByPeriodCsvForMonth3),
        Seq(statementsByPeriodCsvForMonth4)
      )
  }
}
