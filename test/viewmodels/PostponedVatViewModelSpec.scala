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
import models.{PostponedVatStatementFile, PostponedVatStatementGroup}
import models.metadata.PostponedVatStatementFileMetadata
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import play.api.i18n.Messages
import services.DateTimeService
import utils.CommonTestData._
import utils.SpecBase
import utils.Utils.emptyString
import views.helpers.Formatters

import java.time.LocalDate

class PostponedVatViewModelSpec extends SpecBase {

  "CurrentStatementRow.apply" should {

    "produce CurrentStatementRow with correct contents" when {

     "isCdsOnly is true and PostponedVatStatementGroup has statements" in new Setup {

       val pvatStatementGroup: PostponedVatStatementGroup =
         PostponedVatStatementGroup(dateOfPreviousMonth, certificateFiles)

       CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly = true) mustBe
       CurrentStatementRow(
         msgs(Formatters.dateAsMonthAndYear(dateOfPreviousMonth)),
         cdsDDRow = None,
         chiefDDRow = None,
         collapsibleStatementGroupRows = Seq()
       )
     }

      "isCdsOnly is false and PostponedVatStatementGroup has statements" in new Setup {

      }

      "PostponedVatStatementGroup has no statements" in new Setup {

      }

      "PostponedVatStatementGroup has no statements, startDate is of the previous month (after 19th) " +
        "and isCdsOnly is true" in new Setup {

      }

      "PostponedVatStatementGroup has no statements, startDate is of the previous month (after 19th) " +
        "and isCdsOnly is false" in new Setup {

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

    val date: LocalDate = LocalDate.of(YEAR_2023, MONTH_10, DAY_1)
    val dateOfPreviousMonth: LocalDate = date.minusMonths(ONE_MONTH).withDayOfMonth(DAY_20)
    val currentDate: LocalDate = LocalDate.of(YEAR_2023, MONTH_10, DAY_20)

    val dutyPaymentMethodSource: Seq[String] = Seq(CDS, CHIEF)

    val app: Application = application().build()
    implicit val msgs: Messages = messages(app)

    implicit val mockDateTimeService: DateTimeService = mock[DateTimeService]
  }
}
