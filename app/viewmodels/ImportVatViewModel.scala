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
import models.VatCertificatesForEori
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.{
  ddComponent, divComponent, dlComponent, dtComponent, emptyString, h1Component, h2Component, hmrcNewTabLinkComponent,
  linkComponent, pComponent
}
import views.html.components.download_link
import models.FileRole.C79Certificate
import _root_.uk.gov.hmrc.hmrcfrontend.views.html.components.NewTabLink
import utils.Utils
import models.FileFormat.{Csv, Pdf}

case class ImportVatViewModel(
  title: Option[String],
  backLink: Option[String],
  heading: HtmlFormat.Appendable,
  certificateAvailableGuidance: HtmlFormat.Appendable,
  last6MonthsH2Heading: HtmlFormat.Appendable,
  notificationPanel: Option[HtmlFormat.Appendable] = None,
  currentStatements: Seq[ImportVatCurrentStatementRow] = Seq.empty,
  currentStatementsNotAvailableGuidance: Option[HtmlFormat.Appendable] = None,
  certsOlderThan6MonthsGuidance: GuidanceRowWithParagraph,
  chiefDeclarationGuidance: GuidanceRowWithParagraph,
  helpAndSupportGuidance: GuidanceRowWithParagraph
)

object ImportVatViewModel {
  def apply(
    certificatesForAllEoris: Seq[VatCertificatesForEori],
    serviceUnavailableUrl: Option[String]
  )(implicit messages: Messages, config: AppConfig): ImportVatViewModel = {

    val hasRequestedCertificates: Boolean = certificatesForAllEoris.exists(_.requestedCertificates.nonEmpty)
    val hasCurrentCertificates: Boolean   = certificatesForAllEoris.exists(_.currentCertificates.nonEmpty)

    ImportVatViewModel(
      title = Some(messages("cf.account.vat.title")),
      backLink = Some(config.customsFinancialsFrontendHomepage),
      heading = populateHeading(),
      certificateAvailableGuidance = populateCertsAvailableGuidance(),
      last6MonthsH2Heading = populateLast6MonthsH2Heading(),
      notificationPanel = if (hasRequestedCertificates) Some(populateNotificationPanel()) else None,
      currentStatements = if (hasCurrentCertificates) populateCurrentStatements(certificatesForAllEoris) else Seq.empty,
      currentStatementsNotAvailableGuidance =
        if (hasCurrentCertificates) None else Some(populateCurrentStatNotAvailableGuidance),
      certsOlderThan6MonthsGuidance = populateCertsOlderThan6MonthsGuidance(serviceUnavailableUrl),
      chiefDeclarationGuidance = populateChiefDeclarationGuidance,
      helpAndSupportGuidance = populateHelpAndSupportGuidance
    )
  }

  private def populateHeading(implicit messages: Messages): HtmlFormat.Appendable =
    h1Component("cf.account.vat.title", id = Some("import-vat-certificates-heading"), classes = "govuk-heading-xl")

  private def populateCertsAvailableGuidance(implicit messages: Messages): HtmlFormat.Appendable = pComponent(
    "cf.account.vat.available-text"
  )

  private def populateLast6MonthsH2Heading(implicit messages: Messages): HtmlFormat.Appendable = h2Component(
    "cf.account.vat.your-certificates.heading"
  )

  private def populateNotificationPanel(implicit messages: Messages, appConfig: AppConfig): HtmlFormat.Appendable = {
    val htmlContent: Html = HtmlFormat.fill(
      Seq(
        linkComponent(
          linkMessage = messages("cf.import-vat.requested-certificates-available-link-text"),
          location = appConfig.requestedStatements(C79Certificate),
          preLinkMessage = Some(messages("cf.account.detail.requested-certificates-available-text.pre")),
          postLinkMessage = Some(messages("cf.account.detail.requested-certificates-available-text.post")),
          pClass = "govuk-body govuk-!-margin-bottom-1"
        )
      )
    )

    divComponent(
      content = htmlContent,
      classes = Some("notifications-panel"),
      id = Some("notification-panel")
    )
  }

  private def populateCurrentStatements(
    certsForAllEoris: Seq[VatCertificatesForEori]
  )(implicit msgs: Messages): Seq[ImportVatCurrentStatementRow] =
    certsForAllEoris.indices
      .map { historyIndex =>
        if (certsForAllEoris(historyIndex).currentCertificates.nonEmpty) {

          val eoriHeading: Option[HtmlFormat.Appendable] = populateEoriHeading(certsForAllEoris, historyIndex)
          val divContentRows: Seq[HtmlFormat.Appendable] = populateDivComponent(certsForAllEoris, historyIndex)

          val dlComponentRowContent = dlComponent(
            content = HtmlFormat.fill(divContentRows.map(htmlFormat => Html(htmlFormat.body))),
            classes = Some("govuk-summary-list statement-list c79-statements"),
            id = Some(s"statements-list-$historyIndex")
          )

          ImportVatCurrentStatementRow(eoriHeading = eoriHeading, dlComponentRow = dlComponentRowContent)
        } else {
          ImportVatCurrentStatementRow()
        }
      }
      .filterNot(_.eoriHeading.contains(HtmlFormat.empty))

  private def populateCurrentStatNotAvailableGuidance(implicit messages: Messages): HtmlFormat.Appendable =
    pComponent("cf.account.vat.no-certificates-available", id = Some("no-certificates-available-text"))

  private def populateCertsOlderThan6MonthsGuidance(serviceUnavailableUrl: Option[String])(implicit
    messages: Messages
  ): GuidanceRowWithParagraph =
    GuidanceRowWithParagraph(
      h2Heading = h2Component(
        "cf.account.vat.older-certificates.heading",
        id = Some("missing-certificates-guidance-heading"),
        classes = "govuk-heading-m govuk-!-margin-top-9"
      ),
      link = Some(
        linkComponent(
          "cf.account.vat.older-certificates.description.link",
          location = serviceUnavailableUrl.getOrElse(emptyString),
          preLinkMessage = Some("cf.account.vat.older-certificates.description.1")
        )
      )
    )

  private def populateChiefDeclarationGuidance(implicit
    messages: Messages,
    appConfig: AppConfig
  ): GuidanceRowWithParagraph =
    GuidanceRowWithParagraph(
      h2Heading = h2Component(
        "cf.account.vat.chief.heading",
        id = Some("chief-guidance-heading"),
        classes = "govuk-heading-m govuk-!-margin-top-6"
      ),
      link = Some(
        linkComponent(
          appConfig.c79EmailAddress,
          location = appConfig.c79EmailAddressHref,
          preLinkMessage = Some("cf.account.vat.older-certificates.description.2")
        )
      )
    )

  private def populateHelpAndSupportGuidance(implicit
    messages: Messages,
    appConfig: AppConfig
  ): GuidanceRowWithParagraph =
    GuidanceRowWithParagraph(
      h2Heading = h2Component(
        id = Some("vat.support.message.heading"),
        msg = "cf.account.vat.support.heading",
        classes = "govuk-heading-m govuk-!-margin-top-2"
      ),
      paragraph = Some(
        pComponent(
          message = "cf.account.vat.support.message",
          classes = "govuk-body govuk-!-margin-bottom-9",
          tabLink = Some(
            hmrcNewTabLinkComponent(
              NewTabLink(
                language = Some(messages.lang.toString),
                classList = Some("govuk-link"),
                href = Some(appConfig.viewVatAccountSupportLink),
                text = messages("cf.account.vat.support.link")
              )
            )
          )
        )
      )
    )

  private def populateEoriHeading(certsForAllEoris: Seq[VatCertificatesForEori], historyIndex: Int)(implicit
    msgs: Messages
  ): Option[HtmlFormat.Appendable] =
    if (historyIndex > 0) {
      Some(
        h2Component(
          msg = msgs("cf.account.details.previous-eori", certsForAllEoris(historyIndex).eoriHistory.eori),
          id = Some(s"historic-eori-$historyIndex"),
          classes = "govuk-heading-s"
        )
      )
    } else {
      None
    }

  private def populateDivComponent(certsForAllEoris: Seq[VatCertificatesForEori], historyIndex: Int)(implicit
    msgs: Messages
  ): Seq[HtmlFormat.Appendable] =
    certsForAllEoris(historyIndex).currentCertificates.sorted.reverse.zipWithIndex.map {
      (statementsOfOneMonth, index) =>

        val dt = dtComponent(
          content = Html(statementsOfOneMonth.formattedMonthYear),
          classes = Some(s"statements-list-$historyIndex-row-$index-date-cell"),
          id = Some("govuk-summary-list__value")
        )

        val dd = if (statementsOfOneMonth.files.nonEmpty) {
          ddComponent(
            content = HtmlFormat.fill(
              Seq(
                download_link(
                  statementsOfOneMonth.pdf,
                  Pdf,
                  s"statements-list-$historyIndex-row-$index-pdf-download-link",
                  statementsOfOneMonth.formattedMonthYear
                ),
                download_link(
                  statementsOfOneMonth.csv,
                  Csv,
                  s"statements-list-$historyIndex-row-$index-csv-download-link",
                  statementsOfOneMonth.formattedMonthYear
                )
              )
            ),
            classes = Some("govuk-summary-list__actions")
          )
        } else {
          ddComponent(
            content = Html(msgs("cf.account.vat.statements.unavailable", statementsOfOneMonth.formattedMonth)),
            classes = Some("govuk-summary-list__actions")
          )
        }

        divComponent(
          content = HtmlFormat.fill(Seq(dt, dd)),
          classes = Some("govuk-summary-list__row"),
          id = Some(s"statements-list-$historyIndex-row-$index")
        )
    }
}

case class GuidanceRowWithParagraph(
  h2Heading: HtmlFormat.Appendable,
  link: Option[HtmlFormat.Appendable] = None,
  paragraph: Option[HtmlFormat.Appendable] = None
)

case class ImportVatCurrentStatementRow(
  eoriHeading: Option[HtmlFormat.Appendable] = Some(HtmlFormat.empty),
  dlComponentRow: HtmlFormat.Appendable = HtmlFormat.empty
)
