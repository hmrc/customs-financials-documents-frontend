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
import utils.Constants._

class ConstantsSpec extends SpecBase {
  "FIXED_DATE_TIME_YEAR" should {
    "correct value" in {
      FIXED_DATE_TIME_YEAR mustBe 2027
    }
  }

  "FIXED_DATE_TIME_MONTH_OF_YEAR" should {
    "correct value" in {
      FIXED_DATE_TIME_MONTH_OF_YEAR mustBe 12
    }
  }

  "FIXED_DATE_TIME_DAY_OF_MONTH" should {
    "correct value" in {
      FIXED_DATE_TIME_DAY_OF_MONTH mustBe 20
    }
  }

  "FIXED_DATE_TIME_HOUR_OF_DAY" should {
    "correct value" in {
      FIXED_DATE_TIME_HOUR_OF_DAY mustBe 12
    }
  }

  "FIXED_DATE_TIME_MINUTES_OF_HOUR" should {
    "correct value" in {
      FIXED_DATE_TIME_MINUTES_OF_HOUR mustBe 30
    }
  }
}