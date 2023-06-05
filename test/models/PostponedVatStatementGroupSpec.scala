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
import play.api.Application
import utils.SpecBase

import java.time.LocalDate

class PostponedVatStatementGroupSpec extends SpecBase {
  "isPreviousMonthAndAfter14Th" should {
    "return true if statement date is of previous month and is accessed on or after " +
      "15th day of the current month" in new Setup {
      val dateOfPreviousMonth: LocalDate = LocalDate.now.minusMonths(1).withDayOfMonth(16)
      if (LocalDate.now.getDayOfMonth > 14) {
        PostponedVatStatementGroup(dateOfPreviousMonth, Seq())(messages(app)).isPreviousMonthAndAfter14Th mustBe true
      }
    }

    "return false if statement date is of previous month and is accessed on or before " +
      "14th day of the current month" in new Setup {
      val dateOfPreviousMonth: LocalDate = LocalDate.now.minusMonths(1).withDayOfMonth(10)
      PostponedVatStatementGroup(dateOfPreviousMonth, Seq())(messages(app)).isPreviousMonthAndAfter14Th mustBe false
    }

    "return true if statement date is not from the previous month" in new Setup {
      val dateOfCurrentMonth: LocalDate = LocalDate.now.withDayOfMonth(16)
      PostponedVatStatementGroup(dateOfCurrentMonth, Seq())(messages(app)).isPreviousMonthAndAfter14Th mustBe true
    }

    "return true if statement date is not from the previous month and accessed " +
      "before 15th day of current date " in new Setup {
      val dateOfCurrentMonthAndBefore15ThDay: LocalDate = LocalDate.now.withDayOfMonth(12)
      if (LocalDate.now.getDayOfMonth < 15) {
        PostponedVatStatementGroup(
          dateOfCurrentMonthAndBefore15ThDay,
          Seq())(messages(app)).isPreviousMonthAndAfter14Th mustBe true
      }
    }
  }

  trait Setup {
    val app: Application = application().overrides().build()
  }
}
