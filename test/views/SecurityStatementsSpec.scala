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

package views

import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.i18n.Messages
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.securities.security_statements
import viewmodels.SecurityStatementsViewModel
import models.{EoriHistory, SecurityStatementsForEori, SecurityStatementsByPeriod, SecurityStatementFile}
import java.time.LocalDate
import models.FileFormat.{Pdf, Csv}
import models.FileRole.SecurityStatement
import models.metadata.SecurityStatementFileMetadata

class SecurityStatementsSpec extends SpecBase {

  "Security_Statements view" should {
    "display correct title" in new Setup {
      view.title() mustBe "Notification of adjustment statements - View your customs financial accounts - GOV.UK"
    }

    "display correct h1" in new Setup {
      view.getElementsByTag("h1").html() mustBe messages(app)("cf.security-statements.title")
    }

    "display correct h2" in new Setup {
      view.getElementsByTag("h2").html() mustBe "Help make GOV.UK better\nWhy is my statement not available?\nSupport links"
    }
  }

  trait Setup {

    val app: Application = application().build()

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))
    val date: LocalDate = LocalDate.now().withDayOfMonth(28)

    val securityStatementFile: SecurityStatementFile = SecurityStatementFile(
      "statementfile_00", "download_url_00", 99L, SecurityStatementFileMetadata(
        date.minusMonths(1).getYear, date.minusMonths(1).getMonthValue, 28,
        date.getYear, date.getMonthValue, 28, Csv, SecurityStatement,
        "testEori1", 500L, "0000000", None))

    val securityStatementPdfFile: SecurityStatementFile = SecurityStatementFile(
      "statementfile_01", "download_url_00", 99L, SecurityStatementFileMetadata(
        date.minusMonths(1).getYear, date.minusMonths(1).getMonthValue, 28,
        date.getYear, date.getMonthValue, 28, Pdf, SecurityStatement,
        "testEori1", 500L, "0000000", None))

    val securityStatementsByPeriod = SecurityStatementsByPeriod(
      date.minusMonths(1), date, Seq(securityStatementPdfFile, securityStatementFile))

    val securityStatementsForEori = SecurityStatementsForEori(
      eoriHistory.head, Seq(securityStatementsByPeriod), Seq.empty)

    val securityStatementsViewModel = SecurityStatementsViewModel(
      Seq(securityStatementsForEori))

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[security_statements].apply(securityStatementsViewModel).body)
  }
}
