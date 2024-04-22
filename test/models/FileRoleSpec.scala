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

package models

import models.FileRole._
import utils.SpecBase

class FileRoleSpec extends SpecBase {

  "apply" should {

    "return C79Certificate" in {
      val fileRole = FileRole("C79Certificate")
      fileRole shouldEqual C79Certificate
    }

    "return PostponedVATStatement" in {
      val fileRole = FileRole("PostponedVATStatement")
      fileRole shouldEqual PostponedVATStatement
    }

    "return SecurityStatement" in {
      val fileRole = FileRole("SecurityStatement")
      fileRole shouldEqual SecurityStatement
    }

    "return PostponedVATAmendedStatement" in {
      val fileRole = FileRole("PostponedVATAmendedStatement")
      fileRole shouldEqual PostponedVATAmendedStatement
    }

    "throw an exception for invalid role " in {
      assertThrows[Exception] {
        FileRole("UnknownRole")
      }
    }
  }
}
