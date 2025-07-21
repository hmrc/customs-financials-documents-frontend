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

package models.metadata

import models.{FileFormat, FileRole}
import utils.SpecBase
import utils.CommonTestData.{
  CHECK_SUM_000000, DAY_1, DAY_10, EORI_NUMBER, MONTH_1, MONTH_2, SIZE_99L, STATEMENT_REQUEST_ID, YEAR_2018
}

class SdesFileMetadataSpec extends SpecBase {

  "toMap" should {
    "return correct map for SecurityStatementFileMetadata" in {
      val securityStatFileMetaData = SecurityStatementFileMetadata(
        periodStartYear = YEAR_2018,
        periodStartMonth = MONTH_1,
        periodStartDay = DAY_1,
        periodEndYear = YEAR_2018,
        periodEndMonth = MONTH_2,
        periodEndDay = DAY_10,
        fileFormat = FileFormat.Pdf,
        fileRole = FileRole.C79Certificate,
        eoriNumber = EORI_NUMBER,
        fileSize = SIZE_99L,
        checksum = CHECK_SUM_000000,
        statementRequestId = Some(STATEMENT_REQUEST_ID)
      )

      securityStatFileMetaData.toMap shouldBe Map(
        "fileRole"           -> "C79Certificate",
        "checksum"           -> "checksum_0000000",
        "fileSize"           -> "99",
        "periodEndDay"       -> "10",
        "periodStartDay"     -> "1",
        "fileFormat"         -> "PDF",
        "periodEndYear"      -> "2018",
        "eoriNumber"         -> "testEori1",
        "periodStartMonth"   -> "1",
        "periodStartYear"    -> "2018",
        "statementRequestId" -> "Some(statement-request-id)",
        "periodEndMonth"     -> "2"
      )
    }

    "return correct map for PostponedVatStatementFileMetadata" in {
      val pvatStatFileMetaData = PostponedVatStatementFileMetadata(
        periodStartYear = YEAR_2018,
        periodStartMonth = MONTH_1,
        fileFormat = FileFormat.Csv,
        fileRole = FileRole.PostponedVATStatement,
        source = "test_source",
        statementRequestId = Some(STATEMENT_REQUEST_ID)
      )

      pvatStatFileMetaData.toMap shouldBe Map(
        "source"             -> "test_source",
        "fileRole"           -> "PostponedVATStatement",
        "statementRequestId" -> "Some(statement-request-id)",
        "fileFormat"         -> "CSV",
        "periodStartMonth"   -> "1",
        "periodStartYear"    -> "2018"
      )
    }

    "return correct map for VatCertificateFileMetadata" in {
      val vatCertFileMetaData = VatCertificateFileMetadata(
        periodStartYear = YEAR_2018,
        periodStartMonth = MONTH_1,
        fileFormat = FileFormat.Pdf,
        fileRole = FileRole.C79Certificate,
        statementRequestId = Some(STATEMENT_REQUEST_ID)
      )

      vatCertFileMetaData.toMap shouldBe Map(
        "fileRole"           -> "C79Certificate",
        "statementRequestId" -> "Some(statement-request-id)",
        "fileFormat"         -> "PDF",
        "periodStartMonth"   -> "1",
        "periodStartYear"    -> "2018"
      )
    }
  }
}
