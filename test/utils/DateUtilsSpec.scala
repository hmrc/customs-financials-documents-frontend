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

package utils

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import utils.DateUtils.{isDateInLastSixMonths, isDayBefore15ThDayOfTheMonth}

import java.time.LocalDate

class DateUtilsSpec extends SpecBase {

  "isBefore15ThDayOfTheMonth" should {
    "return true when input day is before 15th day of the current month" in {
      val dateBefore15ThDay: LocalDate = LocalDate.of(2023, 5, 12)
      val dateOf14ThDayOfTheMonth: LocalDate = LocalDate.of(2023, 5, 14)

      isDayBefore15ThDayOfTheMonth(dateBefore15ThDay) shouldBe true
      isDayBefore15ThDayOfTheMonth(dateOf14ThDayOfTheMonth) shouldBe true
    }

    "return false when input day is after 15th day of the current month" in {
      val dateAfter15ThDay: LocalDate = LocalDate.of(2023, 4, 20)
      isDayBefore15ThDayOfTheMonth(dateAfter15ThDay) shouldBe false
    }

    "return false when input day is 15th day of the current month" in {
      val dateWith15ThDay: LocalDate = LocalDate.of(2023, 3, 15)
      isDayBefore15ThDayOfTheMonth(dateWith15ThDay) shouldBe false
    }
  }

  "isDateInLastSixMonths" should {
    "return true" when {
      "date is in last 6 months" in {
        val date1 = LocalDate.of(2023, 5, 20)
        val date2 = LocalDate.of(2023, 6, 10)
        val date3 = LocalDate.of(2023, 8, 2)
        val date4 = LocalDate.of(2023, 9, 20)
        val date5 = LocalDate.of(2023, 5, 21)
        val date6 = LocalDate.of(2023, 5, 1)

        val currentDate = LocalDate.of(2023, 11, 20)

        isDateInLastSixMonths(date1, currentDate) mustBe true
        isDateInLastSixMonths(date2, currentDate) mustBe true
        isDateInLastSixMonths(date3, currentDate) mustBe true
        isDateInLastSixMonths(date4, currentDate) mustBe true
        isDateInLastSixMonths(date5, currentDate) mustBe true
        isDateInLastSixMonths(date6, currentDate) mustBe true
      }
    }

    "return false" when {
      "date is not in last 6 months" in {
        val date1 = LocalDate.of(2023, 4, 29)
        val date2 = LocalDate.of(2023, 4, 10)
        val date3 = LocalDate.of(2022, 12, 31)
        val date4 = LocalDate.of(2023, 3, 20)
        val date5 = LocalDate.of(2023, 4, 30)

        val currentDate = LocalDate.of(2023, 11, 20)

        isDateInLastSixMonths(date1, currentDate) mustBe false
        isDateInLastSixMonths(date2, currentDate) mustBe false
        isDateInLastSixMonths(date3, currentDate) mustBe false
        isDateInLastSixMonths(date4, currentDate) mustBe false
        isDateInLastSixMonths(date5, currentDate) mustBe false
      }
    }
  }
}
