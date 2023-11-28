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
import models.FileFormat.{Csv, Pdf}
import models.FileRole.SecurityStatement
import models.{EoriHistory, SecurityStatementFile, SecurityStatementsByPeriod, SecurityStatementsForEori, VatCertificatesByMonth, VatCertificatesForEori}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import viewmodels.SecurityStatementsViewModel
import views.html.securities.security_statements
import models.metadata.SecurityStatementFileMetadata

import java.time.LocalDate

class SecurityStatementsSpec extends SpecBase {

  "view" should {
    "display correct title and guidance" when {
      "statements are available" in new Setup {
        val view: Document =
          Jsoup.parse(app.injector.instanceOf[security_statements].apply(viewModelWithStatements).body)

        view.title() mustBe
          s"${messages(app)("cf.security-statements.title")} - ${messages(app)("service.name")} - GOV.UK"
        view.getElementsByTag("h1").text() mustBe messages(app)("cf.security-statements.title")

        view.getElementById("missing-documents-guidance-heading").text mustBe
          messages(app)("cf.common.missing-documents-guidance.heading",
            messages(app)("cf.common.missing-documents-guidance.statement"))

        view.getElementById("missing-documents-guidance-chiefHeading").text mustBe
          messages(app)("cf.common.missing-documents-guidance.chiefHeading",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("missing-documents-guidance-text1").text mustBe
          messages(app)("cf.common.missing-documents-guidance.text1",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("missing-documents-guidance-certificatesHeading").text mustBe
          messages(app)("cf.common.missing-documents-guidance.subHeading",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("missing-documents-guidance-text2").text mustBe
          messages(app)("cf.common.missing-documents-guidance.text2",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("historic-statement-request").text() mustBe
          messages(app)("cf.security-statements.historic.description")

        view.getElementById("historic-statement-request-link").text() mustBe
          messages(app)("cf.security-statements.historic.request")

        view.text().contains("PDF") mustBe true
        view.text().contains("CSV") mustBe true

        view.text().contains(messages(app)("cf.security-statements.eom")) mustBe true
      }

      "statements are empty" in new Setup {
        val view: Document =
          Jsoup.parse(app.injector.instanceOf[security_statements].apply(viewModelWithNoStatements).body)

        view.title() mustBe
          s"${messages(app)("cf.security-statements.title")} - ${messages(app)("service.name")} - GOV.UK"
        view.getElementsByTag("h1").text() mustBe messages(app)("cf.security-statements.title")

        view.getElementById("missing-documents-guidance-heading").text mustBe
          messages(app)("cf.common.missing-documents-guidance.heading",
            messages(app)("cf.common.missing-documents-guidance.statement"))

        view.getElementById("missing-documents-guidance-chiefHeading").text mustBe
          messages(app)("cf.common.missing-documents-guidance.chiefHeading",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("missing-documents-guidance-text1").text mustBe
          messages(app)("cf.common.missing-documents-guidance.text1",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("missing-documents-guidance-certificatesHeading").text mustBe
          messages(app)("cf.common.missing-documents-guidance.subHeading",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("missing-documents-guidance-text2").text mustBe
          messages(app)("cf.common.missing-documents-guidance.text2",
            messages(app)("cf.common.missing-documents-guidance.statements"))

        view.getElementById("historic-statement-request").text() mustBe
          messages(app)("cf.security-statements.historic.description")

        view.getElementById("historic-statement-request-link").text() mustBe
          messages(app)("cf.security-statements.historic.request")

        view.text().contains(messages(app)("cf.security-statements.eom")) mustBe false
      }
    }
  }

  trait Setup {
    val app: Application = application().build()
    val serviceUnavailableUrl: Option[String] = Option("service_unavailable_url")

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))
    val date: LocalDate = LocalDate.now().withDayOfMonth(28)

    val securityStatementFile: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(
          date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          28, date.getYear, date.getMonthValue, 28, Pdf, SecurityStatement, "testEori1", 500L, "0000000", None))

    val securityStatementFileCsv: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(
          date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          28, date.getYear, date.getMonthValue, 28, Csv, SecurityStatement, "testEori1", 500L, "0000000", None))

    val statementsByPeriod: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(1), date, Seq(securityStatementFile, securityStatementFileCsv))

    val securityStatementsForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(EoriHistory("testEori1", None, None), Seq(statementsByPeriod), Seq.empty)


    val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
      VatCertificatesByMonth(date.minusMonths(1), Seq())(messages(app)),
      VatCertificatesByMonth(date.minusMonths(2), Seq())(messages(app)),
      VatCertificatesByMonth(date.minusMonths(3), Seq())(messages(app)),
      VatCertificatesByMonth(date.minusMonths(4), Seq())(messages(app)),
      VatCertificatesByMonth(date.minusMonths(5), Seq())(messages(app)),
      VatCertificatesByMonth(date.minusMonths(6), Seq())(messages(app)),
    )
    val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
      Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))


    val viewModelWithNoStatements: SecurityStatementsViewModel = SecurityStatementsViewModel(Seq())
    val viewModelWithStatements: SecurityStatementsViewModel = SecurityStatementsViewModel(Seq(securityStatementsForEori))

  }
}
