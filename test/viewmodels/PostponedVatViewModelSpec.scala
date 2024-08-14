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

import models.DutyPaymentMethod.{CDS, CHIEF}
import models.FileFormat.Pdf
import models.FileRole.PostponedVATStatement
import models.metadata.PostponedVatStatementFileMetadata
import models.{PostponedVatStatementFile, PostponedVatStatementGroup}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import play.api.i18n.Messages
import services.DateTimeService
import utils.CommonTestData._
import utils.SpecBase
import utils.Utils.emptyString
import views.helpers.Formatters
import views.html.components.linkInner
import views.html.postponed_vat.{collapsible_statement_group, download_link_pvat_statement}

import java.time.{LocalDate, LocalDateTime}

class PostponedVatViewModelSpec extends SpecBase {

  "CurrentStatementRow.apply" should {

    "produce CurrentStatementRow with correct contents" when {

      "isCdsOnly is true and PostponedVatStatementGroup has statements" in new Setup {

        val isCdsOnly = true

        val pvatStatementGroup: PostponedVatStatementGroup =
          PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, certificateFiles)

        val collapStatGroupRowForSourceCDS: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = Some(new collapsible_statement_group(downloadLinkPvatStatement).apply(
              pvatStatementGroup.collectFiles(amended = false, CDS),
              "cf.account.pvat.download-link",
              "cf.account.pvat.aria.download-link",
              Some("cf.common.not-available"),
              CDS,
              Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate),
              isCdsOnly
            ))
          )

        val collapStatGroupRowForSourceCHIEF: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = None
          )

        val expectedResult: CurrentStatementRow = CurrentStatementRow(
          pvatStatementGroup.periodId,
          msgs(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
          cdsDDRow = None,
          chiefDDRow = None,
          collapsibleStatementGroupRows = Seq(collapStatGroupRowForSourceCDS, collapStatGroupRowForSourceCHIEF)
        )

        CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
      }

      "isCdsOnly is false and PostponedVatStatementGroup has statements" in new Setup {
        val isCdsOnly = false

        val pvatStatementGroup: PostponedVatStatementGroup =
          PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, certificateFiles)

        val collapStatGroupRowForSourceCDS: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = Some(new collapsible_statement_group(downloadLinkPvatStatement).apply(
              pvatStatementGroup.collectFiles(amended = false, CDS),
              "cf.account.pvat.download-link",
              "cf.account.pvat.aria.download-link",
              Some("cf.common.not-available"),
              CDS,
              Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate),
              isCdsOnly
            ))
          )

        val collapStatGroupRowForSourceCHIEF: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = None
          )

        val expectedResult: CurrentStatementRow = CurrentStatementRow(
          pvatStatementGroup.periodId,
          msgs(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
          cdsDDRow = None,
          chiefDDRow = None,
          collapsibleStatementGroupRows = Seq(collapStatGroupRowForSourceCDS, collapStatGroupRowForSourceCHIEF)
        )

        CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
      }

      "PostponedVatStatementGroup has no statements, startDate is of the previous month (after 19th) " +
        "and isCdsOnly is true" in new Setup {

        val isCdsOnly = true

        when(mockDateTimeService.systemDateTime()).thenReturn(date)

        val pvatStatementGroup: PostponedVatStatementGroup =
          PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, Seq())

        val cdsDDRow: DDRow = DDRow(
          msgs("cf.common.not-available"),
          msgs(
            "cf.common.not-available-screen-reader-cds",
            Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate)
          )
        )

        val expectedResult: CurrentStatementRow = CurrentStatementRow(
          pvatStatementGroup.periodId,
          msgs(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
          cdsDDRow = Some(cdsDDRow),
          chiefDDRow = None,
          collapsibleStatementGroupRows = Seq()
        )

        CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
      }

      "PostponedVatStatementGroup has no statements, startDate is of the previous month (after 19th) " +
        "and isCdsOnly is false" in new Setup {

        val isCdsOnly = false

        when(mockDateTimeService.systemDateTime()).thenReturn(date)

        val pvatStatementGroup: PostponedVatStatementGroup =
          PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, Seq())

        val cdsDDRow: DDRow = DDRow(msgs("cf.common.not-available"),
          msgs("cf.common.not-available-screen-reader-cds",
            Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate))
        )

        val chiefDDRow: DDRow = DDRow(msgs("cf.common.not-available"),
          msgs("cf.common.not-available-screen-reader-chief",
            Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate))
        )

        val expectedResult: CurrentStatementRow = CurrentStatementRow(
          pvatStatementGroup.periodId,
          msgs(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
          cdsDDRow = Some(cdsDDRow),
          chiefDDRow = Some(chiefDDRow),
          collapsibleStatementGroupRows = Seq()
        )

        CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
      }

    }
  }

  trait Setup {
    val certificateFiles: Seq[PostponedVatStatementFile] = Seq(
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString)
    )

    val periodId = "test_id"
    val date: LocalDateTime = LocalDateTime.of(YEAR_2023, MONTH_10, DAY_20, HOUR_12, MINUTES_30, SECONDS_50)
    val dateOfPreviousMonthAndAfter19th: LocalDate = date.toLocalDate.minusMonths(ONE_MONTH).withDayOfMonth(DAY_20)
    val currentDate: LocalDate = LocalDate.of(YEAR_2023, MONTH_10, DAY_20)

    val dutyPaymentMethodSource: Seq[String] = Seq(CDS, CHIEF)
    val linkInner = new linkInner()
    val downloadLinkPvatStatement = new download_link_pvat_statement(linkInner)

    val app: Application = application().build()
    implicit val msgs: Messages = messages(app)

    implicit val mockDateTimeService: DateTimeService = mock[DateTimeService]
  }
}
