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
import utils.CommonTestData.{
  DAY_28, EORI_NUMBER, FIVE_MONTHS, FOUR_MONTHS, ONE_MONTH, SIX_MONTHS, SIZE_111L, STAT_FILE_NAME_04, THREE_MONTHS,
  TWO_MONTHS, URL_TEST
}
import utils.SpecBase
import org.scalatest.matchers.must.Matchers.mustBe
import utils.Utils.{
  ddComponent, divComponent, dlComponent, dtComponent, emptyString, h1Component, h2Component, linkComponent, pComponent
}
import models.FileFormat.{Csv, Pdf}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.hmrcfrontend.views.html.components.HmrcNewTabLink
import _root_.uk.gov.hmrc.hmrcfrontend.views.html.components.NewTabLink
import views.html.components.download_link

import java.time.LocalDate
import viewmodels.common.GuidanceRow

class ImportVatViewModelSpec extends SpecBase {

  "ImportVatViewModel.apply" should {

    "create viewModel with correct contents" when {

      "model has current certificates only" in new Setup {
        shouldContainCorrectTitle(viewModelWithCurrentCerts)
        shouldContainCorrectBackLink(viewModelWithCurrentCerts)
        shouldContainCorrectHeading(viewModelWithCurrentCerts)
        shouldContainCorrectCertificateAvailableGuidance(viewModelWithCurrentCerts)
        shouldContainCorrectLast6MonthsHeading(viewModelWithCurrentCerts)
        shouldNotContainNotificationPanel(viewModelWithCurrentCerts)
        shouldContainCurrentStatements(viewModelWithCurrentCerts, certificatesForAllEoris)
        shouldNotContainCurrentStatementsNotAvailableGuidance(viewModelWithCurrentCerts)
        shouldContainCorrectCertsOlderThan6MonthsGuidance(viewModelWithCurrentCerts)
        shouldContainCorrectChiefDeclarationGuidance(viewModelWithCurrentCerts)
        shouldContainCorrectHelpAndSupportGuidance(viewModelWithCurrentCerts)
      }

      "model has both requested and current certificates" in new Setup {
        shouldContainCorrectTitle(viewModel)
        shouldContainCorrectBackLink(viewModel)
        shouldContainCorrectHeading(viewModel)
        shouldContainCorrectCertificateAvailableGuidance(viewModel)
        shouldContainCorrectLast6MonthsHeading(viewModel)
        shouldContainNotificationPanel(viewModel)
        shouldContainCurrentStatements(viewModel, certificatesForAllEoris)
        shouldNotContainCurrentStatementsNotAvailableGuidance(viewModel)
        shouldContainCorrectCertsOlderThan6MonthsGuidance(viewModel)
        shouldContainCorrectChiefDeclarationGuidance(viewModel)
        shouldContainCorrectHelpAndSupportGuidance(viewModel)
      }

      "model has neither requested nor current certificates" in new Setup {
        shouldContainCorrectTitle(viewModelWithNoCerts)
        shouldContainCorrectBackLink(viewModelWithNoCerts)
        shouldContainCorrectHeading(viewModelWithNoCerts)
        shouldContainCorrectCertificateAvailableGuidance(viewModelWithNoCerts)
        shouldContainCorrectLast6MonthsHeading(viewModelWithNoCerts)
        shouldNotContainNotificationPanel(viewModelWithNoCerts)
        shouldNotContainCurrentStatements(viewModelWithNoCerts)
        shouldContainCurrentStatementsNotAvailableGuidance(viewModelWithNoCerts)
        shouldContainCorrectCertsOlderThan6MonthsGuidance(viewModelWithNoCerts)
        shouldContainCorrectChiefDeclarationGuidance(viewModelWithNoCerts)
        shouldContainCorrectHelpAndSupportGuidance(viewModelWithNoCerts)
      }
    }
  }

  private def shouldContainCorrectTitle(viewModel: ImportVatViewModel)(implicit msgs: Messages): Assertion =
    viewModel.title mustBe Some(msgs("cf.account.vat.title"))

  private def shouldContainCorrectBackLink(viewModel: ImportVatViewModel)(implicit config: AppConfig): Assertion =
    viewModel.backLink mustBe Some(config.customsFinancialsFrontendHomepage)

  private def shouldContainCorrectHeading(viewModel: ImportVatViewModel)(implicit msgs: Messages): Assertion =
    viewModel.heading mustBe h1Component(
      "cf.account.vat.title",
      id = Some("import-vat-certificates-heading"),
      classes = "govuk-heading-xl"
    )

  private def shouldContainCorrectCertificateAvailableGuidance(viewModel: ImportVatViewModel)(implicit
    msgs: Messages
  ): Assertion =
    viewModel.certificateAvailableGuidance mustBe pComponent("cf.account.vat.available-text")

  private def shouldContainCorrectLast6MonthsHeading(viewModel: ImportVatViewModel)(implicit
    msgs: Messages
  ): Assertion =
    viewModel.last6MonthsH2Heading mustBe h2Component("cf.account.vat.your-certificates.heading")

  private def shouldContainNotificationPanel(viewModel: ImportVatViewModel) = viewModel.notificationPanel mustBe empty

  private def shouldNotContainNotificationPanel(viewModel: ImportVatViewModel) =
    viewModel.notificationPanel mustBe empty

  private def shouldContainCurrentStatements(
    viewModel: ImportVatViewModel,
    certificatesForAllEoris: Seq[VatCertificatesForEori]
  )(implicit msgs: Messages): Assertion =
    viewModel.currentStatements mustBe expectedImportVatCurrentStatements(certificatesForAllEoris)

  private def shouldNotContainCurrentStatements(viewModel: ImportVatViewModel): Assertion =
    viewModel.currentStatements mustBe Seq.empty

  private def shouldNotContainCurrentStatementsNotAvailableGuidance(viewModel: ImportVatViewModel): Assertion =
    viewModel.currentStatementsNotAvailableGuidance mustBe empty

  private def shouldContainCurrentStatementsNotAvailableGuidance(
    viewModel: ImportVatViewModel
  )(implicit msgs: Messages): Assertion =
    viewModel.currentStatementsNotAvailableGuidance mustBe Some(
      pComponent(
        "cf.account.vat.no-certificates-available",
        id = Some("no-certificates-available-text")
      )
    )

  private def shouldContainCorrectCertsOlderThan6MonthsGuidance(viewModel: ImportVatViewModel)(implicit
    msgs: Messages
  ) =
    viewModel.certsOlderThan6MonthsGuidance mustBe GuidanceRow(
      h2Component(
        "cf.account.vat.older-certificates.heading",
        id = Some("missing-certificates-guidance-heading"),
        classes = "govuk-heading-m govuk-!-margin-top-9"
      ),
      Some(
        linkComponent(
          "cf.account.vat.older-certificates.description.link",
          location = URL_TEST,
          preLinkMessage = Some("cf.account.vat.older-certificates.description.1")
        )
      )
    )

  private def shouldContainCorrectChiefDeclarationGuidance(viewModel: ImportVatViewModel)(implicit
    appConfig: AppConfig,
    msgs: Messages
  ) =
    viewModel.chiefDeclarationGuidance mustBe GuidanceRow(
      h2Component(
        "cf.account.vat.chief.heading",
        id = Some("chief-guidance-heading"),
        classes = "govuk-heading-m govuk-!-margin-top-6"
      ),
      Some(
        linkComponent(
          appConfig.c79EmailAddress,
          location = appConfig.c79EmailAddressHref,
          preLinkMessage = Some("cf.account.vat.older-certificates.description.2")
        )
      )
    )

  private def shouldContainCorrectHelpAndSupportGuidance(
    viewModel: ImportVatViewModel
  )(implicit appConfig: AppConfig, msgs: Messages) =
    viewModel.helpAndSupportGuidance mustBe GuidanceRow(
      h2Heading = h2Component(
        id = Some("vat.support.message.heading"),
        msg = "cf.account.vat.support.heading",
        classes = "govuk-heading-m govuk-!-margin-top-2"
      ),
      paragraph = Some(
        pComponent(
          message = "cf.account.vat.support.message",
          classes = "govuk-body govuk-!-margin-bottom-9",
          tabLink = Some(
            new HmrcNewTabLink().apply(
              NewTabLink(
                language = Some(msgs.lang.toString),
                classList = Some("govuk-link"),
                href = Some(appConfig.viewVatAccountSupportLink),
                text = msgs("cf.account.vat.support.link")
              )
            )
          )
        )
      )
    )

  private def expectedImportVatCurrentStatements(
    certsForAllEoris: Seq[VatCertificatesForEori]
  )(implicit msgs: Messages): List[ImportVatCurrentStatementRow] = {

    val divContentRows = certsForAllEoris.head.currentCertificates.sorted.reverse.zipWithIndex.map {
      (statementsOfOneMonth, index) =>

        val dt = dtComponent(
          content = Html(statementsOfOneMonth.formattedMonthYear),
          classes = Some(s"statements-list-0-row-$index-date-cell"),
          id = Some("govuk-summary-list__value")
        )

        val dd = populateDDComponentForImportVatStatement(statementsOfOneMonth, index)

        divComponent(
          content = HtmlFormat.fill(Seq(dt, dd)),
          classes = Some("govuk-summary-list__row"),
          id = Some(s"statements-list-0-row-$index")
        )
    }

    val dlComponentRow = dlComponent(
      content = HtmlFormat.fill(divContentRows.map(htmlFormat => Html(htmlFormat.body))),
      classes = Some("govuk-summary-list statement-list c79-statements"),
      id = Some(s"statements-list-0")
    )

    List(ImportVatCurrentStatementRow(eoriHeading = None, dlComponentRow = dlComponentRow))
  }

  private def populateDDComponentForImportVatStatement(
    statementsOfOneMonth: VatCertificatesByMonth,
    index: Int
  )(implicit msgs: Messages) =
    if (statementsOfOneMonth.files.nonEmpty) {
      ddComponent(
        content = HtmlFormat.fill(
          Seq(
            download_link(
              statementsOfOneMonth.pdf,
              Pdf,
              s"statements-list-0-row-$index-pdf-download-link",
              statementsOfOneMonth.formattedMonthYear
            ),
            download_link(
              statementsOfOneMonth.csv,
              Csv,
              s"statements-list-0-row-$index-csv-download-link",
              statementsOfOneMonth.formattedMonthYear
            )
          )
        ),
        classes = Some("govuk-summary-list__actions")
      )
    } else {
      ddComponent(
        content = Html(msgs("cf.account.vat.statements.unavailable", statementsOfOneMonth.formattedMonth)),
        classes = Some("govuk-summary-list__actions")
      )
    }

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

    val serviceUnavailableUrl: String = URL_TEST

    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]

    val viewModel: ImportVatViewModel = ImportVatViewModel(certificatesForAllEoris, Some(serviceUnavailableUrl))

    val viewModelWithCurrentCerts: ImportVatViewModel =
      ImportVatViewModel(certificatesForAllEoris, Some(serviceUnavailableUrl))

    val viewModelWithNoCerts: ImportVatViewModel = ImportVatViewModel(Seq.empty, Some(serviceUnavailableUrl))
  }
}
