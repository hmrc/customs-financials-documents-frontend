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

object DateUtils {

  def isDayBefore15ThDayOfTheMonth(date: LocalDate): Boolean = date.getDayOfMonth < 15

  def isDateInLastSixMonths(date: LocalDate, currentDate: LocalDate): Boolean = {
    val dayOne = 1
    val sixMonths = 6

    val currentMonthValue = currentDate.getMonthValue
    val currentYearValue = currentDate.getYear

    val currentDateWithFirstDay = LocalDate.of(currentYearValue, currentMonthValue, dayOne)
    val dateBefore6Months = currentDateWithFirstDay.minusMonths(sixMonths)

    !date.isBefore(dateBefore6Months)
  }
}
