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
import models.SecurityStatementsForEori
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.html.components.{h1, h2Inner, h3Inner, link, missing_documents_guidance, p, pInner, requestedStatements}

case class SecurityStatementsViewModel(statementsForAllEoris: Seq[SecurityStatementsForEori],
                                       hasRequestedStatements: Boolean,
                                       hasCurrentStatements: Boolean,
                                       header: HtmlFormat.Appendable,
                                       requestedStatementNotification: HtmlFormat.Appendable,
                                       requestStatementsLink: HtmlFormat.Appendable,
                                       statementServiceParagraph: HtmlFormat.Appendable,
                                       missingGuidance: HtmlFormat.Appendable)

object SecurityStatementsViewModel {
  def apply(statementsForAllEoris: Seq[SecurityStatementsForEori])(implicit appConfig: AppConfig,
                                                                   messages: Messages): SecurityStatementsViewModel = {

    SecurityStatementsViewModel(
      statementsForAllEoris = statementsForAllEoris,
      hasRequestedStatements = hasRequestedStatements(statementsForAllEoris),
      hasCurrentStatements = hasCurrentStatements(statementsForAllEoris),
      header = generateHeader,
      requestedStatementNotification = generateRequestedStatementNotification(statementsForAllEoris),
      requestStatementsLink = generateRequestStatementLink,
      statementServiceParagraph = generateStatementServiceParagraph,
      missingGuidance = generateMissingGuidance)
  }

  private def generateHeader(implicit messages: Messages): HtmlFormat.Appendable = {
    new h1().apply(
      msg = "cf.security-statements.title",
      classes = "govuk-heading-xl")
  }

  private def generateRequestedStatementNotification(statementsForAllEoris: Seq[SecurityStatementsForEori])
                                                    (implicit appConfig: AppConfig, messages: Messages
                                                    ): HtmlFormat.Appendable = {
    if (hasRequestedStatements(statementsForAllEoris)) {
      new requestedStatements(new link).apply(
        url = appConfig.requestedStatements(SecurityStatement))
    } else {
      HtmlFormat.empty
    }
  }

  private def generateMissingGuidance(implicit messages: Messages): HtmlFormat.Appendable = {
    new missing_documents_guidance(new h2Inner, new h3Inner, new pInner).apply(
      documentType = "statement")
  }

  private def generateStatementServiceParagraph(implicit messages: Messages): HtmlFormat.Appendable = {
    new p().apply(
      message = "cf.security-statements.historic.description",
      id = Some("historic-statement-request"))
  }

  private def generateRequestStatementLink(implicit appConfig: AppConfig, messages: Messages): HtmlFormat.Appendable = {
    new link().apply(
      pClass = "govuk-body govuk-!-margin-bottom-9",
      linkId = Some("historic-statement-request-link"),
      linkClass = "govuk-body govuk-link",
      linkMessage = "cf.security-statements.historic.request",
      location = appConfig.historicRequestUrl(SecurityStatement))
  }

  private def hasRequestedStatements(statementsForAllEoris: Seq[SecurityStatementsForEori]): Boolean =
    statementsForAllEoris.exists(_.requestedStatements.nonEmpty)

  private def hasCurrentStatements(statementsForAllEoris: Seq[SecurityStatementsForEori]): Boolean =
    statementsForAllEoris.exists(_.currentStatements.nonEmpty)
}
