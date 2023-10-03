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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.{Application, inject}
import services.DateTimeService
import utils.SpecBase

import java.time._

class PostponedVatStatementGroupSpec extends SpecBase {
  "isPreviousMonthAndAfter14Th" should {
    "return true if statement date is of previous month and is accessed on or after " +
      "15th day of the current month" in new Setup {
      val dateOfPreviousMonth: LocalDate = date.minusMonths(1).withDayOfMonth(16)
      val currentDate = LocalDate.of(2023,10,16)
      when(mockDateTimeService.systemDateTime())
        .thenReturn(currentDate.atStartOfDay())
      PostponedVatStatementGroup(dateOfPreviousMonth, Seq())(messages(app), mockDateTimeService).isPreviousMonthAndAfter14Th mustBe true
    }

    "return false if statement date is of previous month and is accessed on or before " +
      "14th day of the current month" in new Setup {
      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())
      val dateOfPreviousMonth: LocalDate = date.minusMonths(1).withDayOfMonth(10)
      PostponedVatStatementGroup(dateOfPreviousMonth, Seq())(messages(app), mockDateTimeService).isPreviousMonthAndAfter14Th mustBe false
    }

    "return true if statement date is not from the previous month" in new Setup {
      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())
      val dateOfCurrentMonth: LocalDate = date.withDayOfMonth(16)
      PostponedVatStatementGroup(dateOfCurrentMonth, Seq())(messages(app), mockDateTimeService).isPreviousMonthAndAfter14Th mustBe true
    }

    "return true if statement date is not from the previous month and accessed " +
      "before 15th day of current date " in new Setup {
      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())
      val dateOfCurrentMonthAndBefore15ThDay: LocalDate = date.withDayOfMonth(12)
      if (LocalDate.now.getDayOfMonth < 15) {
        PostponedVatStatementGroup(
          dateOfCurrentMonthAndBefore15ThDay,
          Seq())(messages(app),mockDateTimeService).isPreviousMonthAndAfter14Th mustBe true
      }
    }
  }

  trait Setup {
    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    val date: LocalDate = LocalDate.of(2023,10,1)

    val app: Application = application().overrides(
      inject.bind[DateTimeService].toInstance(mockDateTimeService)
    ).build()
  }
}

