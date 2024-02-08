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

import models.FileFormat.{Csv, Pdf}
import models.FileRole.SecurityStatement
import utils.SpecBase
import models.metadata.SecurityStatementFileMetadata
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import utils.CommonTestData.{
  checkSumValue000000, day28, downloadUrl00, eoriNumber, oneMonth, size500L, size99L,
  statFileName00
}

import java.time.LocalDate

class SecurityStatementsByPeriodSpec extends SpecBase {

  "pdf" should {
    "return Pdf statement file" in new Setup {
      statementsByPeriodForPdf.pdf mustBe Some(securityStatementFilePdf)
    }
  }

  "csv" should {
    "return Csv statement file" in new Setup {
      statementsByPeriodForCsv.csv mustBe Some(securityStatementFileCsv)
    }
  }

  "hasPdf" should {
    "return true if any Pdf file exists" in new Setup {
      statementsByPeriodForPdf.hasPdf mustBe true
    }

    "return false if no Pdf file exists" in new Setup {
      statementsByPeriodForCsv.hasPdf mustBe false
    }
  }

  "hasCsv" should {
    "return true if any Csv file exists" in new Setup {
      statementsByPeriodForCsv.hasCsv mustBe true
    }

    "return false if no Csv file exists" in new Setup {
      statementsByPeriodForPdf.hasCsv mustBe false
    }
  }

  trait Setup {
    val date: LocalDate = LocalDate.now().withDayOfMonth(day28)

    val securityStatementFilePdf: SecurityStatementFile =
      SecurityStatementFile(statFileName00, downloadUrl00, size99L,
        SecurityStatementFileMetadata(
          date.minusMonths(oneMonth).getYear,
          date.minusMonths(oneMonth).getMonthValue,
          day28,
          date.getYear,
          date.getMonthValue,
          day28,
          Pdf,
          SecurityStatement,
          eoriNumber,
          size500L,
          checkSumValue000000,
          None))

    val securityStatementFileCsv: SecurityStatementFile =
      SecurityStatementFile(statFileName00, downloadUrl00, size99L,
        SecurityStatementFileMetadata(
          date.minusMonths(oneMonth).getYear,
          date.minusMonths(oneMonth).getMonthValue,
          day28,
          date.getYear,
          date.getMonthValue,
          day28,
          Csv,
          SecurityStatement,
          eoriNumber,
          size500L,
          checkSumValue000000,
          None))

    val statementsByPeriodForPdf: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(oneMonth), date, Seq(securityStatementFilePdf))

    val statementsByPeriodForCsv: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(oneMonth), date, Seq(securityStatementFileCsv))
  }
}
