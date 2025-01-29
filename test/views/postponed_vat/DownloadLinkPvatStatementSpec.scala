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
import models.metadata.PostponedVatStatementFileMetadata
import models.{FileFormat, PostponedVatStatementFile}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.CommonTestData._
import utils.SpecBase
import utils.Utils.emptyString
import views.html.postponed_vat.download_link_pvat_statement

class DownloadLinkPvatStatementSpec extends SpecBase {

  "view" should {

    "display correct contents" when {

      "Postponed Vat statement files are present" in new Setup {
        val viewDoc: Document = view(Pdf, certificateFiles, downloadLinkMessage, downloadAriaLabel, period)

        val elements: Elements = viewDoc.getElementsByAttribute("href")

        val anchorTag: Element = elements.get(0)

        anchorTag.attr("href") mustBe DOWNLOAD_URL_06
        anchorTag.html().contains("CDS amended statement - PDF (1KB)") mustBe true
        anchorTag.getElementsByClass("govuk-visually-hidden").text mustBe
          "test_period CDS statement - PDF (1KB)"
      }

      "there is no Postponed Vat statement files" in new Setup {
        val viewDoc: Document = view(Pdf, Seq(), downloadLinkMessage, downloadAriaLabel, period)

        viewDoc.body().html() mustBe empty

        val elements: Elements = viewDoc.getElementsByAttribute("href")
        elements.size() mustBe 0
      }
    }
  }

  trait Setup {
    val fileFormat: FileFormat                           = Pdf
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

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    def view(
      fileFormat: FileFormat,
      certificateFiles: Seq[PostponedVatStatementFile] = Seq(),
      downloadLinkMessage: String,
      downloadAriaLabel: String,
      period: String
    ): Document = Jsoup.parse(
      instanceOf[download_link_pvat_statement](application)
        .apply(fileFormat, certificateFiles, downloadLinkMessage, downloadAriaLabel, period)
        .body
    )
  }

}
