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
import play.twirl.api.HtmlFormat
import utils.Utils.{emptyString, h1Component, h2Component, pComponent}

case class GuidanceRowWithParagraph(
  h2Heading: HtmlFormat.Appendable,
  link: Option[HtmlFormat.Appendable] = None,
  paragraph: Option[HtmlFormat.Appendable] = None
)

case class ImportVatViewModel(
  title: Option[String],
  backLink: Option[String],
  heading: HtmlFormat.Appendable,
  certificateAvailableGuidance: HtmlFormat.Appendable,
  last6MonthsH2Heading: HtmlFormat.Appendable,
  notificationPanel: Option[HtmlFormat.Appendable] = None,
  currentStatements: Seq[HtmlFormat.Appendable] = Seq.empty,
  currentStatementsNotAvailableGuidance: Option[HtmlFormat.Appendable] = None,
  certsOlderThan6MonthsGuidance: GuidanceRowWithParagraph,
  chiefDeclarationGuidance: GuidanceRowWithParagraph,
  helpAndSupportGuidance: GuidanceRowWithParagraph
) {
  val hasRequestedCertificates: Boolean = false
  val hasCurrentCertificates: Boolean   = false

  val certificatesForAllEoris: Seq[VatCertificatesForEori] = Seq.empty
}

object ImportVatViewModel {
  def apply(
    certificatesForAllEoris: Seq[VatCertificatesForEori],
    serviceUnavailableUrl: Option[String]
  )(implicit messages: Messages, config: AppConfig): ImportVatViewModel =

    val hasRequestedCertificates: Boolean = certificatesForAllEoris.exists(_.requestedCertificates.nonEmpty)
    val hasCurrentCertificates: Boolean   = certificatesForAllEoris.exists(_.currentCertificates.nonEmpty)

    ImportVatViewModel(
      title = Some(messages("cf.account.vat.title")),
      backLink = Some(config.customsFinancialsFrontendHomepage),
      heading = populateHeading(),
      certificateAvailableGuidance = populateCertsAvailableGuidance(),
      last6MonthsH2Heading = populateLast6MonthsH2Heading(),
      notificationPanel = None,
      currentStatements = Seq.empty,
      currentStatementsNotAvailableGuidance = None,
      certsOlderThan6MonthsGuidance = GuidanceRowWithParagraph(HtmlFormat.empty, Some(HtmlFormat.empty)),
      chiefDeclarationGuidance = GuidanceRowWithParagraph(HtmlFormat.empty, Some(HtmlFormat.empty)),
      helpAndSupportGuidance = GuidanceRowWithParagraph(HtmlFormat.empty, Some(HtmlFormat.empty))
    )

  private def populateHeading(implicit messages: Messages): HtmlFormat.Appendable =
    h1Component("cf.account.vat.title", id = Some("import-vat-certificates-heading"), classes = "govuk-heading-xl")

  private def populateCertsAvailableGuidance(implicit messages: Messages): HtmlFormat.Appendable = pComponent(
    "cf.account.vat.available-text"
  )

  private def populateLast6MonthsH2Heading(implicit messages: Messages): HtmlFormat.Appendable = h2Component(
    "cf.account.vat.your-certificates.heading"
  )

}
