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
