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

package viewmodels

import config.AppConfig
import models.{EoriHistory, VatCertificateFile, VatCertificatesByMonth, VatCertificatesForEori}
import models.metadata.VatCertificateFileMetadata
import models.FileRole.C79Certificate
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import utils.CommonTestData.{DAY_28, EORI_NUMBER, SIZE_111L, STAT_FILE_NAME_04}
import utils.SpecBase
import org.scalatest.matchers.must.Matchers.mustBe
import utils.Utils.{h1Component, h2Component, pComponent}

import java.time.LocalDate

class ImportVatViewModelSpec extends SpecBase {

  "apply" should {

    "create view model with correct contents" in new Setup {
      shouldContainCorrectTitle(viewModel)
      shouldContainCorrectBackLink(viewModel)
      shouldContainCorrectHeading(viewModel)
      shouldContainCorrectCertificateAvailableGuidance(viewModel)
      shouldContainCorrectLast6MonthsHeading(viewModel)
      shouldContainCurrentStatements(viewModel)
      shouldContainCorrectCertsOlderThan6MonthsGuidance(viewModel)
      shouldContainCorrectChiefDeclarationGuidance(viewModel)
      shouldContainCorrectHelpAndSupportGuidance(viewModel)
    }
  }

  private def shouldContainCorrectTitle(viewModel: ImportVatViewModel)(implicit msgs: Messages): Assertion =
    viewModel.title mustBe Some(msgs("cf.account.vat.title"))

  private def shouldContainCorrectBackLink(viewModel: ImportVatViewModel)(implicit config: AppConfig): Assertion =
    viewModel.backLink mustBe Some(config.customsFinancialsFrontendHomepage)

  private def shouldContainCorrectHeading(viewModel: ImportVatViewModel): Assertion =
    viewModel.heading mustBe h1Component(
      "cf.account.vat.title",
      id = Some("import-vat-certificates-heading"),
      classes = "govuk-heading-xl"
    )

  private def shouldContainCorrectCertificateAvailableGuidance(viewModel: ImportVatViewModel): Assertion =
    viewModel.certificateAvailableGuidance mustBe pComponent("cf.account.vat.available-text")

  private def shouldContainCorrectLast6MonthsHeading(viewModel: ImportVatViewModel): Assertion =
    viewModel.last6MonthsH2Heading mustBe h2Component("cf.account.vat.your-certificates.heading")

  private def shouldContainCurrentStatements(viewModel: ImportVatViewModel): Assertion =
    viewModel.currentStatements mustbe Seq.empty

  private def shouldContainCorrectCertsOlderThan6MonthsGuidance(viewModel: ImportVatViewModel) =
    viewModel.certsOlderThan6MonthsGuidance mustBe GuidanceRow()

  private def shouldContainCorrectChiefDeclarationGuidance(viewModel: ImportVatViewModel) =
    viewModel.chiefDeclarationGuidance mustBe GuidanceRow()

  private def shouldContainCorrectHelpAndSupportGuidance(viewModel: ImportVatViewModel) =
    viewModel.helpAndSupportGuidance mustBe GuidanceRow()

  trait Setup {
    val date: LocalDate = LocalDate.now().withDayOfMonth(DAY_28)

    val app: Application        = application().build()
    implicit val msgs: Messages = messages(app)

    val vatCertificateFile1: VatCertificateFile = VatCertificateFile(
      STAT_FILE_NAME_04,
      "download_url_01",
      SIZE_111L,
      VatCertificateFileMetadata(
        date.minusMonths(ONE_MONTH).getYear,
        date.minusMonths(ONE_MONTH).getMonthValue,
        Pdf,
        C79Certificate,
        None
      ),
      emptyString
    )

    val vatCertificateFile2: VatCertificateFile = VatCertificateFile(
      STAT_FILE_NAME_04,
      "download_url_02",
      SIZE_111L,
      VatCertificateFileMetadata(
        date.minusMonths(TWO_MONTHS).getYear,
        date.minusMonths(TWO_MONTHS).getMonthValue,
        Pdf,
        C79Certificate,
        None
      ),
      emptyString
    )

    val vatCertificateFile3: VatCertificateFile = VatCertificateFile(
      STAT_FILE_NAME_04,
      "download_url_03",
      SIZE_111L,
      VatCertificateFileMetadata(
        date.minusMonths(THREE_MONTHS).getYear,
        date.minusMonths(THREE_MONTHS).getMonthValue,
        Pdf,
        C79Certificate,
        None
      ),
      emptyString
    )

    val vatCertificateFile4: VatCertificateFile = VatCertificateFile(
      STAT_FILE_NAME_04,
      "download_url_04",
      SIZE_111L,
      VatCertificateFileMetadata(
        date.minusMonths(FOUR_MONTHS).getYear,
        date.minusMonths(FOUR_MONTHS).getMonthValue,
        Pdf,
        C79Certificate,
        None
      ),
      emptyString
    )

    val vatCertificateFile5: VatCertificateFile = VatCertificateFile(
      STAT_FILE_NAME_04,
      "download_url_05",
      SIZE_111L,
      VatCertificateFileMetadata(
        date.minusMonths(FIVE_MONTHS).getYear,
        date.minusMonths(FIVE_MONTHS).getMonthValue,
        Pdf,
        C79Certificate,
        None
      ),
      emptyString
    )

    val vatCertificateFile6: VatCertificateFile = VatCertificateFile(
      STAT_FILE_NAME_04,
      "download_url_06",
      SIZE_111L,
      VatCertificateFileMetadata(
        date.minusMonths(SIX_MONTHS).getYear,
        date.minusMonths(SIX_MONTHS).getMonthValue,
        Pdf,
        C79Certificate,
        None
      ),
      emptyString
    )

    val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
      VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq(vatCertificateFile1)),
      VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq(vatCertificateFile2)),
      VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq(vatCertificateFile3)),
      VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq(vatCertificateFile4)),
      VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq(vatCertificateFile5)),
      VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq(vatCertificateFile6))
    )

    val eoriHistory: Seq[EoriHistory]                        = Seq(EoriHistory(EORI_NUMBER, None, None))
    val certificatesForAllEoris: Seq[VatCertificatesForEori] = Seq(
      VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty)
    )

    val viewModel: ImportVatViewModel = ImportVatViewModel(certificatesForAllEoris)
    val config: AppConfig             = app.injector.instanceOf[AppConfig]
  }
}
