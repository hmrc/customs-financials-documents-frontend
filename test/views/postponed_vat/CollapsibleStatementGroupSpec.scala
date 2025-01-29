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

package views.postponed_vat

import models.DutyPaymentMethod.{CDS, CHIEF}
import models.FileFormat.Pdf
import models.FileRole.PostponedVATStatement
import models.PostponedVatStatementFile
import models.metadata.PostponedVatStatementFileMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.mustBe

import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.CommonTestData._
import utils.SpecBase
import utils.Utils.emptyString
import views.html.postponed_vat.collapsible_statement_group

class CollapsibleStatementGroupSpec extends SpecBase {

  "view" should {

    "display correct contents" when {

      "Postponed Vat Statement files are present and source is only CDS" in new Setup {
        val viewDoc: Document =
          view(certificateFiles, downloadLinkMessage, downloadAriaLabel, Some(missingFileMessage), CDS, period)

        shouldNotContainMissingFileMessageGuidance(viewDoc)
        shouldContainDownloadLinkPVATStatementSection(viewDoc)
      }

      "Postponed Vat Statement files are present and source include CHIEF" in new Setup {
        val viewDoc: Document = view(
          certificateFiles,
          downloadLinkMessage,
          downloadAriaLabel,
          Some(missingFileMessage),
          CHIEF,
          period,
          isCdsOnly = false
        )

        shouldContainMissingFileMessageGuidance(viewDoc, missingFileMessage, CHIEF, period)
        shouldContainDownloadLinkPVATStatementSection(viewDoc, isCDSOnly = false)
      }

      "there are no Postponed Vat Statement files and source is only CDS" in new Setup {
        val viewDoc: Document =
          view(Seq(), downloadLinkMessage, downloadAriaLabel, Some(missingFileMessage), CDS, period)

        shouldNotDisplayAnyContent(viewDoc)
      }

      "there are no Postponed Vat Statement files and source include CHIEF" in new Setup {
        val viewDoc: Document = view(
          Seq(),
          downloadLinkMessage,
          downloadAriaLabel,
          Some(missingFileMessage),
          CHIEF,
          period,
          isCdsOnly = false
        )

        shouldNotDisplayAnyContent(viewDoc)
      }

      "Postponed Vat Statement files are present, source is only CDS and contains missingFileMessage" in new Setup {
        val viewDoc: Document = view(
          certificateFiles,
          downloadLinkMessage,
          downloadAriaLabel,
          Some(missingFileMessage),
          CHIEF,
          period,
          isCdsOnly = false
        )

        shouldContainMissingFileMessageGuidance(viewDoc, missingFileMessage, CHIEF, period)
      }

      "Postponed Vat Statement files are present, source is only CDS and has no missingFileMessage" in new Setup {
        val viewDoc: Document =
          view(certificateFiles, downloadLinkMessage, downloadAriaLabel, None, CHIEF, period, isCdsOnly = false)

        shouldNotContainMissingFileMessageGuidance(viewDoc)
      }
    }
  }

  private def shouldNotDisplayAnyContent(viewDoc: Document): Assertion = viewDoc.body().text() mustBe empty

  private def shouldContainMissingFileMessageGuidance(
    viewDoc: Document,
    missingFileMessage: String,
    paymentMethodSource: String,
    period: String
  )(implicit msgs: Messages): Assertion = {
    val elements          = viewDoc.getElementsByClass("govuk-summary-list__actions")
    val firstSpanElement  = elements.get(0).getElementsByTag("span").get(0).text()
    val secondSpanElement = elements.get(0).getElementsByTag("span").get(1).text()

    val visuallyHiddenElements = viewDoc.getElementsByClass("govuk-visually-hidden")

    firstSpanElement mustBe messages(missingFileMessage, paymentMethodSource)
    secondSpanElement mustBe messages("cf.common.not-available-screen-reader-cds", period)

    visuallyHiddenElements.size() mustBe 2
  }

  private def shouldNotContainMissingFileMessageGuidance(viewDoc: Document): Assertion = {
    val elements = viewDoc.getElementsByClass("govuk-visually-hidden")

    elements.size() mustBe 1
  }

  private def shouldContainDownloadLinkPVATStatementSection(viewDoc: Document, isCDSOnly: Boolean = true): Assertion = {
    val ddElements = viewDoc.getElementsByTag("dd")

    val ddElementWithDownloadLink = if (isCDSOnly) ddElements.get(0) else ddElements.get(1)

    val anchorTag = ddElementWithDownloadLink.getElementsByTag("a").get(0)

    anchorTag.attr("href") mustBe DOWNLOAD_URL_06
    anchorTag.html().contains("CDS amended statement - PDF (1KB)") mustBe true
    anchorTag.getElementsByClass("govuk-visually-hidden").text mustBe
      "test_period CDS statement - PDF (1KB)"

    if (isCDSOnly) ddElements.size() mustBe 1 else ddElements.size() mustBe 2
  }

  trait Setup {

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val certificateFiles: Seq[PostponedVatStatementFile] = Seq(
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString
      )
    )

    val downloadLinkMessage = "cf.account.pvat.amended-download-link"
    val downloadAriaLabel   = "cf.account.pvat.aria.download-link"
    val period              = "test_period"
    val missingFileMessage  = "cf.common.not-available-screen-reader-cds"

    def view(
      certificateFiles: Seq[PostponedVatStatementFile] = Seq(),
      downloadLinkMessage: String,
      downloadAriaLabel: String,
      missingFileMessage: Option[String] = None,
      dutyPaymentMethodSource: String,
      period: String,
      isCdsOnly: Boolean = true
    ): Document = Jsoup.parse(
      instanceOf[collapsible_statement_group](application)
        .apply(
          certificateFiles,
          downloadLinkMessage,
          downloadAriaLabel,
          missingFileMessage,
          dutyPaymentMethodSource,
          period,
          isCdsOnly
        )
        .body
    )
  }

}
