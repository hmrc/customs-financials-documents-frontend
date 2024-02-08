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
import utils.CommonTestData._
import utils.DateUtils.{isDateInLastSixMonths, isDayBefore15ThDayOfTheMonth}

import java.time.LocalDate

class DateUtilsSpec extends SpecBase {

  "isBefore15ThDayOfTheMonth" should {

    "return true when input day is before 15th day of the current month" in {
      val dateBefore15ThDay: LocalDate = LocalDate.of(year2023, month5, day12)
      val dateOf14ThDayOfTheMonth: LocalDate = LocalDate.of(year2023, month5, day14)

      isDayBefore15ThDayOfTheMonth(dateBefore15ThDay) shouldBe true
      isDayBefore15ThDayOfTheMonth(dateOf14ThDayOfTheMonth) shouldBe true
    }

    "return false when input day is after 15th day of the current month" in {
      val dateAfter15ThDay: LocalDate = LocalDate.of(year2023, month4, day20)

      isDayBefore15ThDayOfTheMonth(dateAfter15ThDay) shouldBe false
    }

    "return false when input day is 15th day of the current month" in {
      val dateWith15ThDay: LocalDate = LocalDate.of(year2023, month3, day15)

      isDayBefore15ThDayOfTheMonth(dateWith15ThDay) shouldBe false
    }
  }

  "isDateInLastSixMonths" should {

    "return true" when {
      "date is in last 6 months" in {
        val date1 = LocalDate.of(year2023, month5, day20)
        val date2 = LocalDate.of(year2023, month6, day10)
        val date3 = LocalDate.of(year2023, month8, day2)
        val date4 = LocalDate.of(year2023, month9, day20)
        val date5 = LocalDate.of(year2023, month5, day21)
        val date6 = LocalDate.of(year2023, month5, day1)

        val currentDate = LocalDate.of(year2023, month11, day20)

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
        val date1 = LocalDate.of(year2023, month4, day29)
        val date2 = LocalDate.of(year2023, month4, day10)
        val date3 = LocalDate.of(year2022, month12, day31)
        val date4 = LocalDate.of(year2023, month3, day20)
        val date5 = LocalDate.of(year2023, month4, day30)

        val currentDate = LocalDate.of(year2023, month11, day20)

        isDateInLastSixMonths(date1, currentDate) mustBe false
        isDateInLastSixMonths(date2, currentDate) mustBe false
        isDateInLastSixMonths(date3, currentDate) mustBe false
        isDateInLastSixMonths(date4, currentDate) mustBe false
        isDateInLastSixMonths(date5, currentDate) mustBe false
      }
    }
  }
}
