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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import utils.SpecBase
import views.helpers.Formatters.fileSize

class FormattersSpec extends SpecBase {

  "Formatters" should {
    "a low kb threshold must display 1KB" in new Setup {
      private val res = fileSize(belowKbThreshold)
      res mustBe "1KB"
    }

    "between kb and mb threshold must display valid KB" in new Setup {
      private val res = fileSize(kbValue)
      res mustBe "29KB"
    }

    "over mb threshold must display valid MB" in new Setup {
      private val res = fileSize(mbValue)
      res mustBe "19.6MB"
    }

    "on threshold for KB" in new Setup {
      private val res = fileSize(kbThreshold)
      res mustBe "1KB"
    }

    "on threshold for MB" in new Setup {
      private val res = fileSize(mbThreshold)
      res mustBe "1.0MB"
    }

    "1kb over threshold for KB" in new Setup {
      private val res = fileSize(kbThreshold + 1024)
      res mustBe "2KB"
    }

    "1mb over threshold for MB" in new Setup {
      private val res = fileSize(mbThreshold + 1024 * 1024)
      res mustBe "2.0MB"
    }
  }

  trait Setup {
    val belowKbThreshold = 100
    val kbValue = 30567
    val mbValue = 20567567

    val kbThreshold = 1024
    val mbThreshold: Int = 1024 * 1024
  }
}
