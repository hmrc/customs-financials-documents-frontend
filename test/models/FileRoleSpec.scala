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

import utils.SpecBase

class FileRoleSpec extends SpecBase {

  "given 'C79Certificate'" should {
    "return C79Certificate" in {
      val fileRole = FileRole("C79Certificate")
      fileRole shouldEqual FileRole.C79Certificate
    }
  }

  "given 'PostponedVATStatement'" should {
    "return PostponedVATStatement" in {
      val fileRole = FileRole("PostponedVATStatement")
      fileRole shouldEqual FileRole.PostponedVATStatement
    }
  }

  "given 'SecurityStatement'" should {
    "return SecurityStatement" in {
      val fileRole = FileRole("SecurityStatement")
      fileRole shouldEqual FileRole.SecurityStatement
    }
  }

  "given 'PostponedVATAmendedStatement'" should {
    "return PostponedVATAmendedStatement" in {
      val fileRole = FileRole("PostponedVATAmendedStatement")
      fileRole shouldEqual FileRole.PostponedVATAmendedStatement
    }
  }

  "given an unknown role" should {
    "throw an exception" in {
      assertThrows[Exception] {
        FileRole("UnknownRole")
      }
    }
  }
}
