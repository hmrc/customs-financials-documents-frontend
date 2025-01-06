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
import models.FileFormat.{Csv, Pdf}
import models.FileRole.SecurityStatement
import models.{SecurityStatementFile, SecurityStatementsByPeriod, SecurityStatementsForEori}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.{
  ddComponent, divComponent, dlComponent, dtComponent, emptyString, h1Component, h2Component, linkComponent, pComponent,
  spanComponent
}
import views.helpers.Formatters.{dateAsDayMonthAndYear, dateAsMonthAndYear}
import views.html.components.{h2Inner, h3Inner, missing_documents_guidance, pInner, requestedStatements}

import java.time.LocalDate

case class SecurityStatementsViewModel(
  pageTitle: Option[String],
  backLink: Option[String],
  header: HtmlFormat.Appendable,
  requestedStatementNotification: HtmlFormat.Appendable,
  currentStatements: HtmlFormat.Appendable,
  missingGuidance: HtmlFormat.Appendable,
  statementServiceParagraph: HtmlFormat.Appendable,
  requestStatementsLink: HtmlFormat.Appendable
)

object SecurityStatementsViewModel {
  def apply(
    statementsForAllEoris: Seq[SecurityStatementsForEori]
  )(implicit appConfig: AppConfig, messages: Messages): SecurityStatementsViewModel =
    SecurityStatementsViewModel(
      pageTitle = Some(messages("cf.security-statements.title")),
      backLink = Some(appConfig.customsFinancialsFrontendHomepage),
      header = generateHeader,
      requestedStatementNotification = generateRequestedStatementNotification(statementsForAllEoris),
      currentStatements = generateCurrentStatements(statementsForAllEoris),
      missingGuidance = generateMissingGuidance,
      statementServiceParagraph = generateStatementServiceParagraph,
      requestStatementsLink = generateRequestStatementLink
    )

  private def generateHeader(implicit messages: Messages): HtmlFormat.Appendable =
    h1Component(msg = "cf.security-statements.title", classes = "govuk-heading-xl")

  private def generateRequestedStatementNotification(
    statementsForAllEoris: Seq[SecurityStatementsForEori]
  )(implicit appConfig: AppConfig, messages: Messages): HtmlFormat.Appendable =
    if (hasRequestedStatements(statementsForAllEoris)) {
      new requestedStatements(linkComponent).apply(url = appConfig.requestedStatements(SecurityStatement))
    } else {
      HtmlFormat.empty
    }

  private def generateCurrentStatements(
    statementsForAllEoris: Seq[SecurityStatementsForEori]
  )(implicit messages: Messages): HtmlFormat.Appendable =
    if (hasCurrentStatements(statementsForAllEoris)) {
      HtmlFormat.fill(populateCurrentPdfAndCsvStatements(statementsForAllEoris))
    } else {
      generateNoStatementsParagraph
    }

  private def populateCurrentPdfAndCsvStatements(
    statementsForAllEoris: Seq[SecurityStatementsForEori]
  )(implicit messages: Messages): Seq[HtmlFormat.Appendable] =
    statementsForAllEoris.zipWithIndex.flatMap {
      case (statementsForEori, historyIndex) if statementsForEori.currentStatements.nonEmpty =>
        val eoriHeader = generateEoriHeader(historyIndex, statementsForEori)
        val eomHeader  = generateEomHeader(statementsForEori)

        val pdfStatements = generateStatements(statementsForEori, historyIndex, isCsv = false)
        val csvStatements = generateStatements(statementsForEori, historyIndex, isCsv = true)

        Seq(eoriHeader, Some(pdfStatements), eomHeader, Some(csvStatements)).flatten

      case _ => Seq.empty
    }

  private def generateEoriHeader(historyIndex: Int, statementsForEori: SecurityStatementsForEori)(implicit
    messages: Messages
  ): Option[HtmlFormat.Appendable] =
    if (historyIndex > 0) {
      Some(
        h2Component(
          msg = messages("cf.account.details.previous-eori", statementsForEori.eoriHistory.eori),
          id = Some(s"historic-eori-$historyIndex"),
          classes = "govuk-heading-s govuk-!-margin-bottom-1"
        )
      )
    } else {
      None
    }

  private def generateEomHeader(
    statementsForEori: SecurityStatementsForEori
  )(implicit messages: Messages): Option[HtmlFormat.Appendable] =
    if (statementsForEori.currentStatements.exists(_.hasCsv)) {
      Some(h2Component(msg = messages("cf.security-statements.eom"), classes = "govuk-heading-m govuk-!-padding-top-2"))
    } else {
      None
    }

  private def generateStatements(statementsForEori: SecurityStatementsForEori, historyIndex: Int, isCsv: Boolean)(
    implicit messages: Messages
  ): HtmlFormat.Appendable = {
    val statementContent = statementsForEori.currentStatements.zipWithIndex.flatMap {
      case (statement, index) if isCsv != statement.hasPdf =>
        Some(generateStatementRow(statement, historyIndex, index, isCsv))
      case _                                               => None
    }

    dlComponent(
      content = HtmlFormat.fill(statementContent),
      id = Some(s"statements-list-$historyIndex${if (isCsv) "-csv" else emptyString}")
    )
  }

  private def generateNoStatementsParagraph(implicit messages: Messages): HtmlFormat.Appendable =
    pComponent(
      message = "cf.security-statements.no-statements",
      classes = "govuk-body govuk-!-margin-top-6 govuk-!-margin-bottom-9 govuk-!-padding-bottom-7",
      id = Some("no-statements")
    )

  private def generateStatementRow(
    statement: SecurityStatementsByPeriod,
    historyIndex: Int,
    index: Int,
    isCsv: Boolean
  )(implicit messages: Messages): HtmlFormat.Appendable = {
    val dateCell   = generateDateCell(statement.startDate, Some(statement.endDate), historyIndex, index, isCsv)
    val linkCell   = generateLinkCell(statement, historyIndex, index, isCsv)
    val rowContent = HtmlFormat.fill(Seq(dateCell, generateDdComponent(linkCell, historyIndex, index, isCsv)))

    divComponent(
      content = rowContent,
      classes = Some("govuk-summary-list__row"),
      id = Some(s"statements-list-$historyIndex-row-$index${if (isCsv) "-csv" else emptyString}")
    )
  }

  private def generateDateCell(
    startDate: LocalDate,
    endDate: Option[LocalDate],
    historyIndex: Int,
    index: Int,
    isCsv: Boolean
  )(implicit messages: Messages): HtmlFormat.Appendable = {
    val dateMessage = if (isCsv) {
      dateAsMonthAndYear(startDate)
    } else {
      messages(
        "cf.security-statements.requested.period",
        dateAsDayMonthAndYear(startDate),
        dateAsDayMonthAndYear(endDate.get)
      )
    }

    dtComponent(
      content = Html(dateMessage),
      classes = Some("govuk-summary-list__value"),
      id = Some(s"statements-list-$historyIndex-row-$index-date-cell${if (isCsv) "-csv" else emptyString}")
    )
  }

  private def generateLinkCell(statement: SecurityStatementsByPeriod, historyIndex: Int, index: Int, isCsv: Boolean)(
    implicit messages: Messages
  ): HtmlFormat.Appendable = {
    val fileType = if (isCsv) statement.csv else statement.pdf
    fileType.fold {
      generateUnavailableLink(statement, historyIndex, index, isCsv)
    } { file =>
      generateAvailableLink(file, statement, isCsv)
    }
  }

  private def generateUnavailableLink(
    statement: SecurityStatementsByPeriod,
    historyIndex: Int,
    index: Int,
    isCsv: Boolean
  )(implicit messages: Messages): HtmlFormat.Appendable = {
    val fileType = if (isCsv) Csv else Pdf

    val dateMessage = if (isCsv) {
      dateAsMonthAndYear(statement.startDate)
    } else {
      s"${dateAsDayMonthAndYear(statement.startDate)} ${messages("cf.security-statements.requested.to")} ${dateAsDayMonthAndYear(statement.endDate)}"
    }

    val ariaLabel = messages(
      if (isCsv) {
        "cf.security-statements.screen-reader.unavailable.month.year"
      } else {
        "cf.security-statements.screen-reader.unavailable"
      },
      fileType,
      dateMessage
    )

    divComponent(
      content = HtmlFormat.fill(
        Seq(
          spanComponent(key = ariaLabel, classes = Some("govuk-visually-hidden")),
          spanComponent(key = messages("cf.unavailable"), ariaHidden = Some("true"))
        )
      ),
      id = Some(s"statements-list-$historyIndex-row-$index-unavailable${if (isCsv) "-csv" else emptyString}")
    )
  }

  private def generateAvailableLink(file: SecurityStatementFile, statement: SecurityStatementsByPeriod, isCsv: Boolean)(
    implicit messages: Messages
  ): HtmlFormat.Appendable = {
    val fileType = if (isCsv) Csv else Pdf

    val dateMessage = if (isCsv) {
      dateAsMonthAndYear(statement.startDate)
    } else {
      s"${dateAsDayMonthAndYear(statement.startDate)} ${messages("cf.security-statements.requested.to")} ${dateAsDayMonthAndYear(statement.endDate)}"
    }

    val ariaLabel = messages(
      if (isCsv) {
        "cf.security-statements.requested.download-link.aria-text.csv"
      } else {
        "cf.security-statements.requested.download-link.aria-text"
      },
      fileType,
      dateMessage,
      file.formattedSize
    )

    linkComponent(
      linkMessage = s"${fileType.toString.toUpperCase} (${file.formattedSize})",
      linkClass = "file-link govuk-link",
      location = file.downloadURL,
      pWrapped = false,
      ariaLabel = Some(ariaLabel)
    )
  }

  private def generateDdComponent(
    linkCell: HtmlFormat.Appendable,
    historyIndex: Int,
    index: Int,
    isCsv: Boolean
  ): HtmlFormat.Appendable =
    ddComponent(
      content = linkCell,
      classes = Some("govuk-summary-list__actions"),
      id = Some(s"statements-list-$historyIndex-row-$index-link-cell${if (isCsv) "-csv" else emptyString}")
    )

  private def generateMissingGuidance(implicit messages: Messages): HtmlFormat.Appendable =
    new missing_documents_guidance(new h2Inner, new h3Inner, new pInner).apply(documentType = "statement")

  private def generateStatementServiceParagraph(implicit messages: Messages): HtmlFormat.Appendable =
    pComponent(message = "cf.security-statements.historic.description", id = Some("historic-statement-request"))

  private def generateRequestStatementLink(implicit appConfig: AppConfig, messages: Messages): HtmlFormat.Appendable =
    linkComponent(
      pClass = "govuk-body govuk-!-margin-bottom-9",
      linkId = Some("historic-statement-request-link"),
      linkClass = "govuk-body govuk-link",
      linkMessage = "cf.security-statements.historic.request",
      location = appConfig.historicRequestUrl(SecurityStatement)
    )

  private def hasRequestedStatements(statementsForAllEoris: Seq[SecurityStatementsForEori]): Boolean =
    statementsForAllEoris.exists(_.requestedStatements.nonEmpty)

  private def hasCurrentStatements(statementsForAllEoris: Seq[SecurityStatementsForEori]): Boolean =
    statementsForAllEoris.exists(_.currentStatements.nonEmpty)
}
