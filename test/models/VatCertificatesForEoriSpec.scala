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
import utils.CommonTestData.{EORI_NUMBER, TEST_LOCAL_DATE}

class VatCertificatesForEoriSpec extends SpecBase {

  "OrderedByEoriHistory" should {

    "order the VatCertificates correctly" in {
      val vatCerts1 =
        VatCertificatesForEori(
          eoriHistory = EoriHistory(EORI_NUMBER, Some(TEST_LOCAL_DATE), Some(TEST_LOCAL_DATE.plusDays(2L))),
          currentCertificates = Seq(),
          requestedCertificates = Seq()
        )

      val vatCerts2 =
        VatCertificatesForEori(
          eoriHistory =
            EoriHistory(EORI_NUMBER, Some(TEST_LOCAL_DATE.plusDays(1L)), Some(TEST_LOCAL_DATE.plusDays(2L))),
          currentCertificates = Seq(),
          requestedCertificates = Seq()
        )

      List(vatCerts1, vatCerts2).sorted shouldBe List(vatCerts2, vatCerts1)
    }
  }
}
