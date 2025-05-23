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

package views.helpers

import org.scalatest.matchers.must.Matchers.mustBe
import utils.SpecBase
import views.helpers.Formatters.fileSize

class FormattersSpec extends SpecBase {

  import Setup.*

  "Formatters" should {
    "display 1KB when fileSize is passed a value below 1KB" in {
      val res = fileSize(belowKbThreshold)
      res mustBe "1KB"
    }

    "display a valid KB when fileSize is passed an amount above 1KB and below 1MB" in {
      val res = fileSize(kbValue)
      res mustBe "29KB"
    }

    "display a valid MB when fileSize is passed an amount above the KB limit" in {
      val res = fileSize(mbValue)
      res mustBe "19.6MB"
    }

    "display 1KB when the fileSize is on the KB threshold" in {
      val res = fileSize(kbThreshold)
      res mustBe "1KB"
    }

    "display 1.0MB when the fileSize is on the MB threshold" in {
      val res = fileSize(mbThreshold)
      res mustBe "1.0MB"
    }

    "display 2KB when fileSize is over the KB threshold by 1" in {
      val res = fileSize(kbThreshold + 1024)
      res mustBe "2KB"
    }

    "display 2.0MB when fileSize is over the MB threshold by 1" in {
      val res = fileSize(mbThreshold + 1024 * 1024)
      res mustBe "2.0MB"
    }
  }

  object Setup {
    val belowKbThreshold = 100
    val kbValue          = 30567
    val mbValue          = 20567567

    val kbThreshold      = 1024
    val mbThreshold: Int = 1024 * 1024
  }
}
