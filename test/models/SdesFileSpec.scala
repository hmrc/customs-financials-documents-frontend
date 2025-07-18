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

import models.DutyPaymentMethod.CDS
import models.FileFormat.{Csv, Pdf}
import models.FileRole.{C79Certificate, PostponedVATStatement}
import models.metadata.{PostponedVatStatementFileMetadata, VatCertificateFileMetadata}
import utils.CommonTestData.{DOWNLOAD_URL_06, MONTH_3, MONTH_4, SIZE_111L, STAT_FILE_NAME_04, YEAR_2018}
import utils.SpecBase
import utils.Utils.emptyString

class SdesFileSpec extends SpecBase {

  "PostponedVatStatementFile" should {

    "ordered correctly as per metadata.fileFormat" in {
      val pVatStatFile1 = PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString
      )

      val pVatStatFile2 = PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Csv, PostponedVATStatement, CDS, None),
        emptyString
      )

      List(pVatStatFile1, pVatStatFile2).sorted shouldBe List(pVatStatFile2, pVatStatFile1)
    }
  }

  "VatCertificateFile" should {

    "ordered correctly as per metadata.fileFormat" in {
      val vatCertFile1 = VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        VatCertificateFileMetadata(YEAR_2018, MONTH_4, Pdf, C79Certificate, None),
        emptyString
      )

      val vatCertFile2 = VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        VatCertificateFileMetadata(YEAR_2018, MONTH_4, Csv, C79Certificate, None),
        emptyString
      )

      List(vatCertFile1, vatCertFile2).sorted shouldBe List(vatCertFile2, vatCertFile1)
    }
  }
}
