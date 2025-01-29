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

package views.securities

import config.AppConfig
import models.FileFormat.{Csv, Pdf, UnknownFileFormat}
import models.FileRole.SecurityStatement
import models.*
import models.metadata.SecurityStatementFileMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.CommonTestData.{
  CHECK_SUM_000000, DAY_28, DOWNLOAD_URL_00, EORI_NUMBER, ONE_MONTH, SIZE_500L, SIZE_99L, STAT_FILE_NAME_00
}
import utils.SpecBase
import viewmodels.SecurityStatementsViewModel
import views.helpers.Formatters.{dateAsDayMonthAndYear, dateAsMonthAndYear, fileSize}
import views.html.securities.security_statements
import play.api.Application

import java.time.LocalDate

class SecurityStatementsSpec extends SpecBase with GuiceOneAppPerSuite {

  "view" should {

    "display correct title and guidance" when {

      "statements are available" in new Setup {
        val view: Document =
          Jsoup.parse(instanceOf[security_statements](app).apply(viewModelWithStatements).body)

        commonGuidanceText(view, app)

        view.text().contains("PDF") mustBe true
        view.text().contains("CSV") mustBe true

        view.text().contains(messages("cf.security-statements.eom")) mustBe true
      }

      "statements are empty" in new Setup {
        val view: Document =
          Jsoup.parse(instanceOf[security_statements](app).apply(viewModelWithNoStatements).body)

        commonGuidanceText(view, app)

        view.text().contains(messages("cf.security-statements.eom")) mustBe false
      }

      "current statements are empty" in new Setup {
        val view: Document =
          Jsoup.parse(instanceOf[security_statements](app).apply(viewModelWithNoCurrentStatements).body)

        commonGuidanceText(view, app)

        view.getElementById("no-statements").text() mustBe messages("cf.security-statements.no-statements")
        view.text().contains(messages("cf.security-statements.eom")) mustBe false
        view.text().contains("PDF") mustBe false
        view.text().contains("CSV") mustBe false
      }

      "statements have Pdfs but not Csvs" in new Setup {
        val view: Document =
          Jsoup.parse(instanceOf[security_statements](app).apply(viewModelWithPdfStatementsOnly).body)

        commonGuidanceText(view, app)

        view.text().contains(messages("cf.security-statements.eom")) mustBe false

        view.text().contains("PDF") mustBe true
        view.text().contains("CSV") mustBe false

        view
          .text()
          .contains(
            messages(
              "cf.security-statements.requested.period",
              dateAsDayMonthAndYear(statementsByPeriodForPdf.startDate),
              dateAsDayMonthAndYear(statementsByPeriodForPdf.endDate)
            )
          ) mustBe true
      }

      "statements have Csvs but not Pdfs" in new Setup {
        val view: Document =
          Jsoup.parse(instanceOf[security_statements](app).apply(viewModelWithCsvStatementsOnly).body)

        commonGuidanceText(view, app)

        view.text().contains(messages("cf.security-statements.eom")) mustBe true

        view.text().contains("PDF") mustBe false
        view.text().contains("CSV") mustBe true

        view.text().contains(dateAsMonthAndYear(statementsByPeriodForCsv.startDate)) mustBe true

        view
          .text()
          .contains(
            messages(
              "cf.security-statements.requested.download-link.aria-text.csv",
              Csv,
              dateAsMonthAndYear(statementsByPeriodForCsv.startDate),
              fileSize(securityStatementFileCsv.metadata.fileSize)
            )
          ) mustBe true
      }

      "statements have Csv(with Unknown file type) but not Pdfs" in new Setup {
        val view: Document =
          Jsoup.parse(
            instanceOf[security_statements](app)
              .apply(viewModelWithCsvStatementsOnlyWithUnknownFileType)
              .body
          )

        commonGuidanceText(view, app)

        view.text().contains("CSV") mustBe true
        val unavailableCsvElem: Element   = view.getElementById("statements-list-0-row-0-unavailable-csv")
        val screenReaderElement: Elements = unavailableCsvElem.getElementsByClass("govuk-visually-hidden")

        screenReaderElement.text() mustBe
          messages(
            "cf.security-statements.screen-reader.unavailable.month.year",
            Csv,
            dateAsMonthAndYear(statementsByPeriodForCsvWithUnknownFileType.startDate)
          )

        view.text().contains(dateAsMonthAndYear(statementsByPeriodForCsvWithUnknownFileType.startDate)) mustBe true
        view.text().contains(messages("cf.unavailable")) mustBe true
      }

      "statements are available and EORI header is present" in new Setup {
        val multipleEoris: Seq[SecurityStatementsForEori] = Seq(
          securityStatementsForEori,
          securityStatementsForEori.copy(eoriHistory = EoriHistory("testEori", None, None))
        )

        val viewModel: SecurityStatementsViewModel = SecurityStatementsViewModel(multipleEoris)
        val view: Document                         = Jsoup.parse(instanceOf[security_statements](app).apply(viewModel).body)

        view.text().contains(messages("cf.account.details.previous-eori", "testEori"))
      }
    }

    "return PDF statements when CSV statements are empty" in new Setup {
      val statementsWithMixedCurrent: Seq[SecurityStatementsForEori] = Seq(
        securityStatementsForEori.copy(currentStatements = Seq.empty),
        securityStatementsForEori.copy(currentStatements = Seq(statementsByPeriodForPdf))
      )

      val viewModel: SecurityStatementsViewModel = SecurityStatementsViewModel(statementsWithMixedCurrent)
      val view: Document                         = Jsoup.parse(instanceOf[security_statements](app).apply(viewModel).body)

      view.text().contains("PDF") mustBe true
      view.text().contains("CSV") mustBe false
    }
  }

  private def commonGuidanceText(view: Document, app: Application): Assertion = {
    view.title() mustBe
      s"${messages("cf.security-statements.title")} - ${messages("service.name")} - GOV.UK"
    view.getElementsByTag("h1").text() mustBe messages("cf.security-statements.title")

    view.getElementById("missing-documents-guidance-heading").text mustBe
      messages(
        "cf.common.missing-documents-guidance.heading",
        messages("cf.common.missing-documents-guidance.statement")
      )

    view.getElementById("missing-documents-guidance-chiefHeading").text mustBe
      messages(
        "cf.common.missing-documents-guidance.chiefHeading",
        messages("cf.common.missing-documents-guidance.statements")
      )

    view.getElementById("missing-documents-guidance-text1").text mustBe
      messages(
        "cf.common.missing-documents-guidance.text1",
        messages("cf.common.missing-documents-guidance.statements")
      )

    view.getElementById("missing-documents-guidance-certificatesHeading").text mustBe
      messages(
        "cf.common.missing-documents-guidance.subHeading",
        messages("cf.common.missing-documents-guidance.statements")
      )

    view.getElementById("missing-documents-guidance-text2").text mustBe
      messages(
        "cf.common.missing-documents-guidance.text2",
        messages("cf.common.missing-documents-guidance.statements")
      )

    view.getElementById("historic-statement-request").text() mustBe
      messages("cf.security-statements.historic.description")

    view.getElementById("historic-statement-request-link").text() mustBe
      messages("cf.security-statements.historic.request")
  }

  override def fakeApplication(): Application = applicationBuilder.build()

  trait Setup {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val date: LocalDate = LocalDate.now().withDayOfMonth(DAY_28)

    val securityStatementFilePdf: SecurityStatementFile =
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

    val securityStatementFileCsv: SecurityStatementFile =
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
          Csv,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val securityStatementFileCsvWithUnknownFileType: SecurityStatementFile =
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
          UnknownFileFormat,
          SecurityStatement,
          EORI_NUMBER,
          SIZE_500L,
          CHECK_SUM_000000,
          None
        )
      )

    val statementsByPeriodForPdf: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(ONE_MONTH), date, Seq(securityStatementFilePdf))

    val statementsByPeriodForCsv: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(ONE_MONTH), date, Seq(securityStatementFileCsv))

    val statementsByPeriodForCsvWithUnknownFileType: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(ONE_MONTH), date, Seq(securityStatementFileCsvWithUnknownFileType))

    val securityStatementsForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(
        EoriHistory(EORI_NUMBER, None, None),
        Seq(statementsByPeriodForPdf, statementsByPeriodForCsv),
        Seq.empty
      )

    val securityStatementsForEoriPdfsOnly: SecurityStatementsForEori =
      SecurityStatementsForEori(EoriHistory(EORI_NUMBER, None, None), Seq(statementsByPeriodForPdf), Seq.empty)

    val securityStatementsForEoriCsvsOnly: SecurityStatementsForEori =
      SecurityStatementsForEori(EoriHistory(EORI_NUMBER, None, None), Seq(statementsByPeriodForCsv), Seq.empty)

    val securityStatementsForEoriCsvsOnlyWithUnknownFileType: SecurityStatementsForEori =
      SecurityStatementsForEori(
        EoriHistory(EORI_NUMBER, None, None),
        Seq(statementsByPeriodForCsvWithUnknownFileType),
        Seq.empty
      )

    val viewModelWithNoStatements: SecurityStatementsViewModel        = SecurityStatementsViewModel(Seq())
    val viewModelWithNoCurrentStatements: SecurityStatementsViewModel =
      SecurityStatementsViewModel(Seq(securityStatementsForEoriPdfsOnly.copy(currentStatements = Seq())))

    val viewModelWithPdfStatementsOnly: SecurityStatementsViewModel =
      SecurityStatementsViewModel(Seq(securityStatementsForEoriPdfsOnly))

    val viewModelWithCsvStatementsOnly: SecurityStatementsViewModel =
      SecurityStatementsViewModel(Seq(securityStatementsForEoriCsvsOnly))

    val viewModelWithCsvStatementsOnlyWithUnknownFileType: SecurityStatementsViewModel =
      SecurityStatementsViewModel(Seq(securityStatementsForEoriCsvsOnlyWithUnknownFileType))

    val viewModelWithStatements: SecurityStatementsViewModel =
      SecurityStatementsViewModel(Seq(securityStatementsForEori))
  }
}
