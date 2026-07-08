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

import models.DutyPaymentMethod.CDS
import models.FileFormat.Pdf
import models.FileRole.PostponedVATStatement
import models.PostponedVatStatementFile
import models.metadata.PostponedVatStatementFileMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.CommonTestData.*
import utils.SpecBase
import utils.Utils.emptyString
import views.html.postponed_vat.collapsible_statement_group

class CollapsibleStatementGroupSpec extends SpecBase with GuiceOneAppPerSuite {

  "view" should {

    "display correct contents" when {

      "Postponed Vat Statement files are present and source is CDS" in new Setup {
        val viewDoc: Document =
          view(certificateFiles, downloadLinkMessage, downloadAriaLabel, Some(missingFileMessage), CDS, period)

        shouldNotContainMissingFileMessageGuidance(viewDoc)
        shouldContainDownloadLinkPVATStatementSection(viewDoc)
      }

      "there are no Postponed Vat Statement files and source is CDS" in new Setup {
        val viewDoc: Document =
          view(Seq(), downloadLinkMessage, downloadAriaLabel, Some(missingFileMessage), CDS, period)

        shouldNotDisplayAnyContent(viewDoc)
      }

      "Postponed Vat Statement files are present with no missingFileMessage" in new Setup {
        val viewDoc: Document =
          view(certificateFiles, downloadLinkMessage, downloadAriaLabel, None, CDS, period)

        shouldNotContainMissingFileMessageGuidance(viewDoc)
      }
    }
  }

  private def shouldNotDisplayAnyContent(viewDoc: Document): Assertion = viewDoc.body().text() mustBe empty

  private def shouldNotContainMissingFileMessageGuidance(viewDoc: Document): Assertion = {
    val elements = viewDoc.getElementsByClass("govuk-visually-hidden")

    elements.size() mustBe 1
  }

  private def shouldContainDownloadLinkPVATStatementSection(viewDoc: Document): Assertion = {
    val ddElements                = viewDoc.getElementsByTag("dd")
    val ddElementWithDownloadLink = ddElements.get(0)

    val anchorTag = ddElementWithDownloadLink.getElementsByTag("a").get(0)

    anchorTag.attr("href") mustBe DOWNLOAD_URL_06
    anchorTag.html().contains("CDS amended statement - PDF (1KB)") mustBe true
    anchorTag.getElementsByClass("govuk-visually-hidden").text mustBe
      "test_period CDS statement - PDF (1KB)"

    ddElements.size() mustBe 1
  }

  override def fakeApplication(): Application = applicationBuilder.build()

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
      period: String
    ): Document = Jsoup.parse(
      instanceOf[collapsible_statement_group](app)
        .apply(
          certificateFiles,
          downloadLinkMessage,
          downloadAriaLabel,
          missingFileMessage,
          dutyPaymentMethodSource,
          period
        )
        .body
    )
  }

}
