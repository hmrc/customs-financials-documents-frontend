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

import java.time.LocalDate
import utils.DateUtils.isDayBefore15ThDayOfTheMonth

class DateUtilsSpec extends SpecBase {

  "isBefore15ThDayOfTheMonth" should {
    "return true when input day is before 15th day of the current month" in {
      val dateBefore15ThDay: LocalDate = LocalDate.of(2023, 5, 12)
      isDayBefore15ThDayOfTheMonth(dateBefore15ThDay) shouldBe true
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
}
