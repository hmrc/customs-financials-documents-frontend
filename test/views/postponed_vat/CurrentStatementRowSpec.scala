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
import models.{PostponedVatStatementFile, PostponedVatStatementGroup}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.mustBe

import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.DateTimeService
import utils.CommonTestData._
import utils.SpecBase
import utils.Utils.emptyString
import viewmodels.{CollapsibleStatementGroupRow, CurrentStatementRow, DDRow}
import views.helpers.Formatters
import views.html.components.linkInner
import views.html.postponed_vat.{collapsible_statement_group, current_statement_row, download_link_pvat_statement}

import java.time.{LocalDate, LocalDateTime}

class CurrentStatementRowSpec extends SpecBase {

  "view" should {

    "display correct contents" when {

      "collapsibleStatementGroupRows are empty but contains dd row for CDS and CHIEF" in new Setup {

        val cdsDDRow: DDRow   = DDRow(notAvailableMsg = notAvailableMsg, visuallyHiddenMsg = visuallyHiddenMsg)
        val chiefDDRow: DDRow = DDRow(notAvailableMsg = notAvailableMsg, visuallyHiddenMsg = visuallyHiddenMsg)

        val statementRow: CurrentStatementRow =
          CurrentStatementRow(periodId, startDateMsg, Some(cdsDDRow), Some(chiefDDRow))

        val viewDoc: Document = view(statementRow)

        shouldDisplayPeriodId(viewDoc, periodId)
        shouldDisplayStartDateMsg(viewDoc, startDateMsg)
        shouldDisplayCDSDDRow(viewDoc, notAvailableMsg, visuallyHiddenMsg)
        shouldDisplayCHIEFDDRow(viewDoc, notAvailableMsg, visuallyHiddenMsg)
      }

      "collapsibleStatementGroupRows are present but no dd row for CDS and CHIEF" in new Setup {

        val statementRow: CurrentStatementRow = CurrentStatementRow(
          periodId,
          startDateMsg,
          cdsDDRow = None,
          chiefDDRow = None,
          collapsibleStatementGroupRows = collapsibleStatementGroupRows
        )

        val viewDoc: Document = view(statementRow)

        shouldDisplayPeriodId(viewDoc, periodId)
        shouldDisplayStartDateMsg(viewDoc, startDateMsg)
        shouldNotDisplayCDsAndCHIEFDDRows(viewDoc, notAvailableMsg, visuallyHiddenMsg)
        shouldDisplayCollapsibleRows(viewDoc, isCdsOnly)
      }
    }
  }

  private def shouldDisplayPeriodId(viewDoc: Document, periodId: String): Assertion =
    viewDoc.getElementById(periodId).html() should not be empty

  private def shouldDisplayStartDateMsg(viewDoc: Document, startDateMsg: String): Assertion =
    viewDoc.getElementsByTag("dt").text() mustBe startDateMsg

  private def shouldDisplayCDSDDRow(
    viewDoc: Document,
    notAvailableMsg: String,
    visuallyHiddenMsg: String
  ): Assertion = {
    val ddElements   = viewDoc.getElementsByTag("dd")
    val cdsDDElement = ddElements.get(0)

    cdsDDElement.html().contains(notAvailableMsg) mustBe true
    cdsDDElement.html().contains(visuallyHiddenMsg) mustBe true
  }

  private def shouldDisplayCHIEFDDRow(
    viewDoc: Document,
    notAvailableMsg: String,
    visuallyHiddenMsg: String
  ): Assertion = {
    val ddElements     = viewDoc.getElementsByTag("dd")
    val chiefDDElement = ddElements.get(1)

    chiefDDElement.html().contains(notAvailableMsg) mustBe true
    chiefDDElement.html().contains(visuallyHiddenMsg) mustBe true
  }

  private def shouldNotDisplayCDsAndCHIEFDDRows(
    viewDoc: Document,
    notAvailableMsg: String,
    visuallyHiddenMsg: String
  ): Assertion = {
    val ddElements = viewDoc.getElementsByTag("dd")

    val cdsDDElement   = ddElements.get(0)
    val chiefDDElement = ddElements.get(1)

    cdsDDElement.html().contains(notAvailableMsg) mustBe false
    cdsDDElement.html().contains(visuallyHiddenMsg) mustBe false

    chiefDDElement.html().contains(notAvailableMsg) mustBe false
    chiefDDElement.html().contains(visuallyHiddenMsg) mustBe false
  }

  private def shouldDisplayCollapsibleRows(viewDoc: Document, isCDSOnly: Boolean): Assertion = {

    val ddElements = viewDoc.getElementsByTag("dd")

    val ddElementWithDownloadLink = if (isCDSOnly) ddElements.get(0) else ddElements.get(1)

    val anchorTag = ddElementWithDownloadLink.getElementsByTag("a").get(0)

    anchorTag.attr("href") mustBe DOWNLOAD_URL_06
    anchorTag.html().contains("CDS - PDF (1KB)") mustBe true
    anchorTag.getElementsByClass("govuk-visually-hidden").text mustBe
      "September 2023 CDS statement - PDF (1KB)"
  }

  trait Setup {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val periodId     = "test_id"
    val startDateMsg = "test_date_msg"

    val notAvailableMsg   = "Not available"
    val visuallyHiddenMsg = "Not available visually hidden"

    def view(currentStatementRow: CurrentStatementRow): Document =
      Jsoup.parse(application.injector.instanceOf[current_statement_row].apply(currentStatementRow).body)

    implicit val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val certificateFiles: Seq[PostponedVatStatementFile] = Seq(
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString
      )
    )

    val isCdsOnly = false

    val date: LocalDateTime                        = LocalDateTime.of(YEAR_2023, MONTH_10, DAY_20, HOUR_12, MINUTES_30, SECONDS_50)
    val dateOfPreviousMonthAndAfter19th: LocalDate = date.toLocalDate.minusMonths(ONE_MONTH).withDayOfMonth(DAY_20)

    val linkInner                 = new linkInner()
    val downloadLinkPvatStatement = new download_link_pvat_statement(linkInner)

    val pvatStatementGroup: PostponedVatStatementGroup =
      PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, certificateFiles)

    val collapStatGroupRowForSourceCDS: CollapsibleStatementGroupRow =
      CollapsibleStatementGroupRow(
        collapsiblePVATAmendedStatement = None,
        collapsiblePVATStatement = Some(
          new collapsible_statement_group(downloadLinkPvatStatement).apply(
            pvatStatementGroup.collectFiles(amended = false, CDS),
            "cf.account.pvat.download-link",
            "cf.account.pvat.aria.download-link",
            Some("cf.common.not-available"),
            CDS,
            Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate),
            isCdsOnly
          )
        )
      )

    val collapStatGroupRowForSourceCHIEF: CollapsibleStatementGroupRow =
      CollapsibleStatementGroupRow(
        collapsiblePVATAmendedStatement = None,
        collapsiblePVATStatement = None
      )

    val collapsibleStatementGroupRows: Seq[CollapsibleStatementGroupRow] =
      Seq(collapStatGroupRowForSourceCDS, collapStatGroupRowForSourceCHIEF)
  }
}
