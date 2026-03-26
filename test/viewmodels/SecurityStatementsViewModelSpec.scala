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

package viewmodels

import config.AppConfig
import models.FileRole.SecurityStatement
import models.{EoriHistory, SecurityStatementsByPeriod, SecurityStatementsForEori}
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.Mockito.when
import play.twirl.api.HtmlFormat
import utils.CommonTestData.{DAY_28, MONTH_11, YEAR_2019}
import utils.SpecBase
import utils.Utils.{dlComponent, h1Component, linkComponent, pComponent}
import views.html.components.{h2Inner, link, missing_documents_guidance, pInner, requestedStatements}

import java.time.LocalDate

class SecurityStatementsViewModelSpec extends SpecBase {

  "SecurityStatementsViewModel" should {

    "produce SecurityStatementsViewModel with correct contents" when {

      "statementsForAllEoris has requested and current statements, last statement has no files" in new Setup {
        result.pageTitle mustBe Some(messages("cf.security-statements.title"))
        result.backLink mustBe Some(appConfig.customsFinancialsFrontendHomepage)
        result.header mustBe expectedHeader
        result.requestedStatementNotification mustBe expectedRequestedNotification
        result.currentStatements mustBe expectedCurrentStatements
        result.missingGuidance mustBe expectedMissingGuidance
      }

      "last statement has empty files, it is excluded from rows" in new Setup {
        val body = result.currentStatements.body
        body.contains("statements-list-0-row-0") mustBe false
      }
    }
  }

  trait Setup {
    implicit val appConfig: AppConfig = mock[AppConfig]

    val date: LocalDate = LocalDate.of(YEAR_2019, MONTH_11, DAY_28)

    val currentStatement: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(startDate = date, endDate = date, files = Seq())

    val requestedStatement: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(startDate = date, endDate = date, files = Seq())

    when(appConfig.customsFinancialsFrontendHomepage).thenReturn("home-page")
    when(appConfig.historicRequestUrl(SecurityStatement)).thenReturn("request-statement-url")
    when(appConfig.requestedStatements(SecurityStatement)).thenReturn("request-statement")

    val expectedHeader: HtmlFormat.Appendable =
      h1Component(msg = "cf.security-statements.title", classes = "govuk-heading-xl")

    val expectedRequestedNotification: HtmlFormat.Appendable =
      new requestedStatements(linkComponent).apply("request-statement")

    val expectedCurrentStatements: HtmlFormat.Appendable = HtmlFormat.fill(
      Seq(
        dlComponent(
          content = HtmlFormat.empty,
          classes = Some("govuk-summary-list statement-list"),
          id = Some("statements-list-0")
        ),
        dlComponent(
          content = HtmlFormat.empty,
          classes = Some("govuk-summary-list statement-list"),
          id = Some("statements-list-0-csv")
        )
      )
    )

    val expectedNoStatementsParagraph: HtmlFormat.Appendable =
      pComponent("cf.security-statements.no-statements", "govuk-body")

    val expectedMissingGuidance: HtmlFormat.Appendable =
      new missing_documents_guidance(new h2Inner, new link, new pInner).apply("statement")

    val statementsForAllEoris: Seq[SecurityStatementsForEori] = Seq(
      SecurityStatementsForEori(
        currentStatements = Seq(currentStatement),
        requestedStatements = Seq(requestedStatement),
        eoriHistory = EoriHistory("GB12345789", None, None)
      )
    )

    val result: SecurityStatementsViewModel = SecurityStatementsViewModel(statementsForAllEoris)
  }
}
