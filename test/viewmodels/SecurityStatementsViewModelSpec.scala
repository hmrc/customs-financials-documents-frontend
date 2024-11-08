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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.CommonTestData.DAY_28
import utils.SpecBase
import utils.Utils.{
  ddComponent, divComponent, dlComponent, dtComponent, h1Component,
  linkComponent, pComponent, spanComponent
}
import views.html.components.{h2Inner, h3Inner, missing_documents_guidance, pInner, requestedStatements}

import java.time.LocalDate

class SecurityStatementsViewModelSpec extends SpecBase {

  "SecurityStatementsViewModel" should {

    "produce SecurityStatementsViewModel with correct contents" when {

      "statementsForAllEoris has requested and current statements" in new Setup {
        result.pageTitle mustBe Some(message("cf.security-statements.title"))
        result.backLink mustBe Some(appConfig.customsFinancialsFrontendHomepage)
        result.header mustBe expectedHeader
        result.requestedStatementNotification mustBe expectedRequestedNotification
        result.currentStatements mustBe expectedCurrentStatements
        result.missingGuidance mustBe expectedMissingGuidance
        result.statementServiceParagraph mustBe expectedStatementServiceParagraph
        result.requestStatementsLink mustBe expectedRequestStatementsLink
      }
    }
  }

  trait Setup {
    implicit val appConfig: AppConfig = mock[AppConfig]
    val app: Application = application().build()
    implicit val message: Messages = messages(app)

    val date: LocalDate = LocalDate.now().withDayOfMonth(DAY_28)

    val currentStatement: SecurityStatementsByPeriod = SecurityStatementsByPeriod(
      startDate = date,
      endDate = date,
      files = Seq())

    val requestedStatement: SecurityStatementsByPeriod = SecurityStatementsByPeriod(
      startDate = date,
      endDate = date,
      files = Seq())

    when(appConfig.customsFinancialsFrontendHomepage).thenReturn("home-page")
    when(appConfig.historicRequestUrl(SecurityStatement)).thenReturn("request-statement-url")
    when(appConfig.requestedStatements(SecurityStatement)).thenReturn("request-statement")

    val expectedHeader: HtmlFormat.Appendable =
      h1Component(msg = "cf.security-statements.title", classes = "govuk-heading-xl")

    val expectedRequestedNotification: HtmlFormat.Appendable =
      new requestedStatements(linkComponent).apply("request-statement")

    val expectedCurrentStatements: HtmlFormat.Appendable = HtmlFormat.fill(Seq(
      dlComponent(
        content = HtmlFormat.empty,
        classes = Some("govuk-summary-list statement-list"),
        id = Some("statements-list-0")),
      createDetailedList()))

    private def createDetailedList(): HtmlFormat.Appendable = {
      dlComponent(
        content = HtmlFormat.fill(Seq(createSummaryListRow())),
        classes = Some("govuk-summary-list statement-list"),
        id = Some("statements-list-0-csv"))
    }

    private def createSummaryListRow(): HtmlFormat.Appendable = {
      divComponent(
        content = HtmlFormat.fill(Seq(createDateCell(), createUnavailableLinkCell())),
        classes = Some("govuk-summary-list__row"),
        id = Some("statements-list-0-row-0-csv"))
    }

    private def createDateCell(): HtmlFormat.Appendable = {
      dtComponent(
        content = Html("November 2024"),
        classes = Some("govuk-summary-list__value"),
        id = Some("statements-list-0-row-0-date-cell-csv"))
    }

    private def createUnavailableLinkCell(): HtmlFormat.Appendable = {
      ddComponent(
        content = divComponent(
          content = createUnavailableCsvCell(),
          id = Some("statements-list-0-row-0-unavailable-csv")
        ),
        classes = Some("govuk-summary-list__actions"),
        id = Some("statements-list-0-row-0-link-cell-csv"))
    }

    private def createUnavailableCsvCell(): HtmlFormat.Appendable = {
      HtmlFormat.fill(Seq(
        spanComponent(key = "CSV for November 2024 unavailable", classes = Some("govuk-visually-hidden")),
        spanComponent(key = "Unavailable", ariaHidden = Some("true"))))
    }

    val expectedNoStatementsParagraph: HtmlFormat.Appendable = pComponent(
      "cf.security-statements.no-statements", "govuk-body")

    val expectedMissingGuidance: HtmlFormat.Appendable = new missing_documents_guidance(
      new h2Inner, new h3Inner, new pInner).apply("statement")

    val expectedStatementServiceParagraph: HtmlFormat.Appendable = pComponent(
      "cf.security-statements.historic.description", "govuk-body", Some("historic-statement-request"))

    val expectedRequestStatementsLink: HtmlFormat.Appendable =
      linkComponent(
        linkMessage = "cf.security-statements.historic.request",
        location = "request-statement-url",
        pClass = "govuk-body govuk-!-margin-bottom-9",
        linkClass = "govuk-body govuk-link",
        linkId = Some("historic-statement-request-link"))

    val statementsForAllEoris: Seq[SecurityStatementsForEori] = Seq(SecurityStatementsForEori(
      currentStatements = Seq(currentStatement),
      requestedStatements = Seq(requestedStatement),
      eoriHistory = EoriHistory("GB12345789", None, None)))

    val result: SecurityStatementsViewModel = SecurityStatementsViewModel(statementsForAllEoris)
  }
}
