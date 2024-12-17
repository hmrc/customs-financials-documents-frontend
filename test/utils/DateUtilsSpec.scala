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

import org.scalatest.matchers.must.Matchers.mustBe
import utils.CommonTestData._
import utils.DateUtils.{isDateInLastSixMonths, isDayBefore20ThDayOfTheMonth}

import java.time.LocalDate

class DateUtilsSpec extends SpecBase {

  "isDayBefore20ThDayOfTheMonth" should {

    "return true when input day is before 20th day of the current month" in {
      val dateBefore20ThDay: LocalDate       = LocalDate.of(YEAR_2023, MONTH_5, DAY_12)
      val dateOf14ThDayOfTheMonth: LocalDate = LocalDate.of(YEAR_2023, MONTH_5, DAY_14)
      val dateOf19ThDayOfTheMonth: LocalDate = LocalDate.of(YEAR_2023, MONTH_5, DAY_19)

      isDayBefore20ThDayOfTheMonth(dateBefore20ThDay)       shouldBe true
      isDayBefore20ThDayOfTheMonth(dateOf14ThDayOfTheMonth) shouldBe true
      isDayBefore20ThDayOfTheMonth(dateOf19ThDayOfTheMonth) shouldBe true
    }

    "return false when input day is after 19th day of the current month" in {
      val dateAfter19ThDay: LocalDate = LocalDate.of(YEAR_2023, MONTH_4, DAY_20)

      isDayBefore20ThDayOfTheMonth(dateAfter19ThDay) shouldBe false
    }

    "return false when input day is 20th day of the current month" in {
      val dateWith20ThDay: LocalDate = LocalDate.of(YEAR_2023, MONTH_3, DAY_20)

      isDayBefore20ThDayOfTheMonth(dateWith20ThDay) shouldBe false
    }
  }

  "isDateInLastSixMonths" should {

    "return true" when {
      "date is in last 6 months" in {
        val date1 = LocalDate.of(YEAR_2023, MONTH_5, DAY_20)
        val date2 = LocalDate.of(YEAR_2023, MONTH_6, DAY_10)
        val date3 = LocalDate.of(YEAR_2023, MONTH_8, DAY_2)
        val date4 = LocalDate.of(YEAR_2023, MONTH_9, DAY_20)
        val date5 = LocalDate.of(YEAR_2023, MONTH_5, DAY_21)
        val date6 = LocalDate.of(YEAR_2023, MONTH_5, DAY_1)

        val currentDate = LocalDate.of(YEAR_2023, MONTH_11, DAY_20)

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
        val date1 = LocalDate.of(YEAR_2023, MONTH_4, DAY_29)
        val date2 = LocalDate.of(YEAR_2023, MONTH_4, DAY_10)
        val date3 = LocalDate.of(YEAR_2022, MONTH_12, DAY_31)
        val date4 = LocalDate.of(YEAR_2023, MONTH_3, DAY_20)
        val date5 = LocalDate.of(YEAR_2023, MONTH_4, DAY_30)

        val currentDate = LocalDate.of(YEAR_2023, MONTH_11, DAY_20)

        isDateInLastSixMonths(date1, currentDate) mustBe false
        isDateInLastSixMonths(date2, currentDate) mustBe false
        isDateInLastSixMonths(date3, currentDate) mustBe false
        isDateInLastSixMonths(date4, currentDate) mustBe false
        isDateInLastSixMonths(date5, currentDate) mustBe false
      }
    }
  }
}
