package utils

import java.time.LocalDate

object DateUtils {

  /**
   * Checks whether the input date's day of Month is before 15th day of the Month
   *
   * @param date: LocalDate
   * @return Boolean
   */
  def isDayBefore15ThDayOfTheMonth(date: LocalDate): Boolean = date.getDayOfMonth < 15
}
