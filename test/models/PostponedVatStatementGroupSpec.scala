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

package models

import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.Mockito.when
import services.DateTimeService
import utils.CommonTestData.*
import utils.SpecBase

import java.time.*

class PostponedVatStatementGroupSpec extends SpecBase {

  "isPreviousMonthAndAfter19Th" should {

    "return true if statement date is of previous month and is accessed on or after " +
      "19th day of the current month" in new Setup {
        val dateOfPreviousMonth: LocalDate = date.minusMonths(ONE_MONTH).withDayOfMonth(DAY_20)
        val currentDate: LocalDate         = LocalDate.of(YEAR_2023, MONTH_10, DAY_20)

        when(mockDateTimeService.systemDateTime()).thenReturn(currentDate.atStartOfDay())

        PostponedVatStatementGroup(dateOfPreviousMonth, Seq())(
          messages,
          mockDateTimeService
        ).isPreviousMonthAndAfter19Th mustBe true
      }

    "return false if statement date is of previous month and is accessed on or before " +
      "19th day of the current month" in new Setup {
        val dateOfPreviousMonth: LocalDate = date.minusMonths(ONE_MONTH).withDayOfMonth(DAY_10)

        when(mockDateTimeService.systemDateTime())
          .thenReturn(date.atStartOfDay())

        PostponedVatStatementGroup(dateOfPreviousMonth, Seq())(
          messages,
          mockDateTimeService
        ).isPreviousMonthAndAfter19Th mustBe false
      }

    "return true if statement date is not from the previous month" in new Setup {
      val dateOfCurrentMonth: LocalDate = date.withDayOfMonth(DAY_16)

      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())

      PostponedVatStatementGroup(dateOfCurrentMonth, Seq())(
        messages,
        mockDateTimeService
      ).isPreviousMonthAndAfter19Th mustBe true
    }

    "return true if statement date is not from the previous month and accessed " +
      "before 19th day of current date " in new Setup {

        val isCurrentDayBefore20: Boolean = LocalDate.now.getDayOfMonth < DAY_20

        if (isCurrentDayBefore20) when(mockDateTimeService.systemDateTime()).thenReturn(date.atStartOfDay())

        val dateOfCurrentMonthAndBefore20ThDay: LocalDate = date.withDayOfMonth(DAY_17)

        if (isCurrentDayBefore20) {
          PostponedVatStatementGroup(dateOfCurrentMonthAndBefore20ThDay, Seq())(
            messages,
            mockDateTimeService
          ).isPreviousMonthAndAfter19Th mustBe true
        }
      }
  }

  trait Setup {
    val date: LocalDate                      = LocalDate.of(YEAR_2023, MONTH_10, DAY_1)
    val mockDateTimeService: DateTimeService = mock[DateTimeService]
  }
}
