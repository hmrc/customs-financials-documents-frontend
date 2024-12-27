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

import models.VatCertificatesForEori
import play.twirl.api.HtmlFormat
import utils.Utils.emptyString

case class GuidanceRow(h2Heading: HtmlFormat.Appendable, link: Option[HtmlFormat.Appendable] = None)

case class ImportVatViewModel(
  title: String,
  backLink: Option[String],
  heading: String,
  certificateAvailableGuidance: HtmlFormat.Appendable,
  last6MonthsHeading: HtmlFormat.Appendable,
  currentStatements: Seq[HtmlFormat.Appendable],
  certsOlderThan6MonthsGuidance: GuidanceRow,
  chiefDeclarationGuidance: GuidanceRow,
  helpAndSupportGuidance: GuidanceRow
)

object ImportVatViewModel {
  def apply(certificatesForAllEoris: Seq[VatCertificatesForEori]): ImportVatViewModel =

    val hasRequestedCertificates: Boolean = certificatesForAllEoris.exists(_.requestedCertificates.nonEmpty)
    val hasCurrentCertificates: Boolean   = certificatesForAllEoris.exists(_.currentCertificates.nonEmpty)

    ImportVatViewModel(
      title = emptyString,
      backLink = Some(emptyString),
      heading = emptyString,
      certificateAvailableGuidance = HtmlFormat.empty,
      last6MonthsHeading = HtmlFormat.empty,
      currentStatements = Seq.empty,
      certsOlderThan6MonthsGuidance = GuidanceRow(HtmlFormat.empty, Some(HtmlFormat.empty)),
      chiefDeclarationGuidance = GuidanceRow(HtmlFormat.empty, Some(HtmlFormat.empty)),
      helpAndSupportGuidance = GuidanceRow(HtmlFormat.empty, Some(HtmlFormat.empty))
    )
}
