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

package controllers

import config.AppConfig
import connectors.{FinancialsApiConnector, SdesConnector}
import models.FileFormat.Pdf
import models.FileRole.C79Certificate
import models.metadata.VatCertificateFileMetadata
import models.{EoriHistory, VatCertificateFile, VatCertificatesByMonth, VatCertificatesForEori}
import navigation.Navigator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.anyString
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.test.Helpers.*
import play.api.{Application, inject}
import utils.CommonTestData.{
  DAY_28, DOWNLOAD_URL_06, DOWNLOAD_URL_07, EORI_NUMBER, FIVE_MONTHS, FOUR_MONTHS, ONE_MONTH, SEVEN_MONTHS, SIX_MONTHS,
  SIZE_111L, STAT_FILE_NAME_04, STAT_FILE_NAME_05, THREE_MONTHS, TWO_MONTHS
}
import utils.Utils.emptyString
import utils.{DateUtils, SpecBase}
import viewmodels.ImportVatViewModel
import views.helpers.Formatters
import views.html.import_vat.{import_vat, import_vat_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class VatControllerSpec extends SpecBase {

  "showVatAccount" should {
    "redirect to certificates unavailable page if getting files fails" in new Setup {

      appConfigForSetup.historicStatementsEnabled = false

      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad(navigator.importVatPageId).url

      val vatCertificateFile: VatCertificateFile = VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
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

      val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
        VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq(vatCertificateFile)),
        VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())
      )

      val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
        Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

      val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, Some(serviceUnavailableUrl))

      when(mockSdesConnector.getVatCertificates(any)(any, any))
        .thenReturn(Future.successful(Seq(vatCertificateFile)))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
        val result  = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(viewModel)(request, messages, appConfigForSetup).toString()
      }
    }

    "display the page correctly when files retrieved" in new Setup {
      when(mockSdesConnector.getVatCertificates(any)(any, any))
        .thenReturn(Future.failed(new RuntimeException("Something went wrong")))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.VatController.certificatesUnavailablePage().url
      }
    }

    "display only last 6 months certs' rows when previous cert files are retrieved in SDES" in new Setup {

      appConfigForSetup.historicStatementsEnabled = false

      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("import-vat").url

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

      val vatCertificateFile7: VatCertificateFile = VatCertificateFile(
        STAT_FILE_NAME_04,
        "download_url_07",
        SIZE_111L,
        VatCertificateFileMetadata(
          date.minusMonths(SEVEN_MONTHS).getYear,
          date.minusMonths(SEVEN_MONTHS).getMonthValue,
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

      val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
        Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

      val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, Some(serviceUnavailableUrl))

      when(mockSdesConnector.getVatCertificates(anyString)(any, any))
        .thenReturn(
          Future.successful(
            Seq(
              vatCertificateFile1,
              vatCertificateFile2,
              vatCertificateFile3,
              vatCertificateFile4,
              vatCertificateFile5,
              vatCertificateFile6,
              vatCertificateFile7
            )
          )
        )

      when(mockFinancialsApiConnector.deleteNotification(any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
        val result  = route(app, request).value

        status(result) mustBe OK

        if (DateUtils.isDayBefore20ThDayOfTheMonth(LocalDate.now())) {
          contentAsString(result) mustBe
            view(viewModel)(request, messages, appConfigForSetup).toString()

          val doc = Jsoup.parse(contentAsString(result))

          doc.getElementById("statements-list-0-row-0-pdf-download-link").attr("href") should
            be("download_url_01")
          doc.getElementById("statements-list-0-row-1-pdf-download-link").attr("href") should
            be("download_url_02")
          doc.getElementById("statements-list-0-row-2-pdf-download-link").attr("href") should
            be("download_url_03")
          doc.getElementById("statements-list-0-row-3-pdf-download-link").attr("href") should
            be("download_url_04")
          doc.getElementById("statements-list-0-row-4-pdf-download-link").attr("href") should
            be("download_url_05")
          doc.getElementById("statements-list-0-row-5-pdf-download-link").attr("href") should
            be("download_url_06")
        }
      }
    }

    "display the cert unavailable text for the relevant month when cert files are retrieved " +
      "after 19th of the month and cert is not available" in new Setup {

        val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
          VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq()),
          VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())
        )

        val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
          Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

        val viewModel: ImportVatViewModel =
          ImportVatViewModel(vatCertificatesForEoris, Some(appConfig.historicRequestUrl(C79Certificate)))

        when(mockSdesConnector.getVatCertificates(anyString)(any, any))
          .thenReturn(Future.successful(Seq()))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        running(app) {
          val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
          val result  = route(app, request).value

          status(result) mustBe OK

          if (!DateUtils.isDayBefore20ThDayOfTheMonth(LocalDate.now())) {
            contentAsString(result) mustBe view(viewModel)(
              request,
              messages,
              appConfigForSetup
            ).toString()

            val doc = Jsoup.parse(contentAsString(result))

            Option(doc.getElementById("statements-list-0-row-5")) should not be empty

            doc.getElementById("statements-list-0-row-0").children().text() should
              include(
                messages(
                  "cf.account.vat.statements.unavailable",
                  Formatters.dateAsMonth(date.minusMonths(ONE_MONTH))
                )
              )
          }
        }
      }

    "not display the cert row for the immediate previous month when cert files are retrieved " +
      "before 20th of the month and cert is not available for immediate previous month" in new Setup {

        appConfigForSetup.historicStatementsEnabled = false

        val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("import-vat").url

        val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
          VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())
        )

        val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
          Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

        val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, Some(serviceUnavailableUrl))

        when(mockSdesConnector.getVatCertificates(anyString)(any, any))
          .thenReturn(Future.successful(Seq()))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        running(app) {
          val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
          val result  = route(app, request).value

          status(result) mustBe OK

          if (DateUtils.isDayBefore20ThDayOfTheMonth(LocalDate.now())) {
            contentAsString(result) mustBe view(viewModel)(
              request,
              messages,
              appConfigForSetup
            ).toString()

            val doc = Jsoup.parse(contentAsString(result))

            Option(doc.getElementById("statements-list-0-row-5")) mustBe empty
            Option(doc.getElementById("statements-list-0-row-0")) should not be empty
            Option(doc.getElementById("statements-list-0-row-1")) should not be empty
            Option(doc.getElementById("statements-list-0-row-2")) should not be empty
            Option(doc.getElementById("statements-list-0-row-3")) should not be empty
            Option(doc.getElementById("statements-list-0-row-4")) should not be empty

            doc.getElementById("statements-list-0-row-0").children().text() should not include messages(
              "cf.account.vat.statements.unavailable",
              Formatters.dateAsMonth(date.minusMonths(ONE_MONTH))
            )
          }
        }
      }

    "display all the certs' row when cert files are retrieved before 20th of the month and " +
      "cert is available" in new Setup {

        appConfigForSetup.historicStatementsEnabled = false

        val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("import-vat").url

        val vatCertificateFile: VatCertificateFile = VatCertificateFile(
          STAT_FILE_NAME_04,
          "download_url_06",
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

        val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
          VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq(vatCertificateFile)),
          VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())
        )

        val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
          Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

        val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, Some(serviceUnavailableUrl))

        when(mockSdesConnector.getVatCertificates(anyString)(any, any))
          .thenReturn(Future.successful(Seq(vatCertificateFile)))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        running(app) {
          val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
          val result  = route(app, request).value

          status(result) mustBe OK

          if (DateUtils.isDayBefore20ThDayOfTheMonth(LocalDate.now())) {
            contentAsString(result) mustBe
              view(viewModel)(request, messages, appConfigForSetup).toString()

            val doc = Jsoup.parse(contentAsString(result))

            Option(doc.getElementById("statements-list-0-row-0")) should not be empty
            Option(doc.getElementById("statements-list-0-row-1")) should not be empty
            Option(doc.getElementById("statements-list-0-row-2")) should not be empty
            Option(doc.getElementById("statements-list-0-row-3")) should not be empty
            Option(doc.getElementById("statements-list-0-row-4")) should not be empty
            Option(doc.getElementById("statements-list-0-row-5")) should not be empty

            doc.getElementById("statements-list-0-row-0").children().text() should not include messages(
              "cf.account.vat.statements.unavailable",
              Formatters.dateAsMonth(date.minusMonths(ONE_MONTH))
            )
          }
        }
      }

    "display historic statements Url when feature is enabled" in new Setup {

      appConfigForSetup.historicStatementsEnabled = true
      val historicRequestUrl: String = appConfigForSetup.historicRequestUrl(C79Certificate)

      val vatCertificateFile: VatCertificateFile = VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
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

      val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
        VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq(vatCertificateFile)),
        VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq()),
        VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())
      )

      val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
        Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

      val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, Some(historicRequestUrl))

      when(mockSdesConnector.getVatCertificates(anyString)(any, any))
        .thenReturn(Future.successful(Seq(vatCertificateFile)))

      when(mockFinancialsApiConnector.deleteNotification(any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
        val result  = route(app, request).value

        status(result) mustBe OK

        if (DateUtils.isDayBefore20ThDayOfTheMonth(LocalDate.now())) {
          contentAsString(result) mustBe
            view(viewModel)(request, messages, appConfigForSetup).toString()
        }
      }
    }

    "display requested statements Url when regular and " +
      "requested statements are present in the same period" in new Setup {

        appConfigForSetup.historicStatementsEnabled = true

        val historicRequestUrl: String    = appConfigForSetup.historicRequestUrl(C79Certificate)
        val someRequestId: Option[String] = Some("statement-request-id")

        val vatCertificateFile: VatCertificateFile = VatCertificateFile(
          STAT_FILE_NAME_04,
          DOWNLOAD_URL_06,
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

        val vatCertificateFile_2: VatCertificateFile = VatCertificateFile(
          STAT_FILE_NAME_05,
          DOWNLOAD_URL_07,
          SIZE_111L,
          VatCertificateFileMetadata(
            date.minusMonths(SEVEN_MONTHS).getYear,
            date.minusMonths(SEVEN_MONTHS).getMonthValue,
            Pdf,
            C79Certificate,
            None
          ),
          emptyString
        )

        val vatCertificateFile_3: VatCertificateFile = VatCertificateFile(
          STAT_FILE_NAME_05,
          DOWNLOAD_URL_07,
          SIZE_111L,
          VatCertificateFileMetadata(
            date.minusMonths(SEVEN_MONTHS).getYear,
            date.minusMonths(SEVEN_MONTHS).getMonthValue,
            Pdf,
            C79Certificate,
            someRequestId
          ),
          emptyString
        )

        val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
          VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq(vatCertificateFile)),
          VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq()),
          VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())
        )

        val requestedCertificates: Seq[VatCertificatesByMonth] = Seq(
          VatCertificatesByMonth(date.minusMonths(SEVEN_MONTHS), Seq(vatCertificateFile_3))
        )

        val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
          Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, requestedCertificates))

        val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, Some(historicRequestUrl))

        when(mockSdesConnector.getVatCertificates(anyString)(any, any))
          .thenReturn(Future.successful(Seq(vatCertificateFile, vatCertificateFile_2, vatCertificateFile_3)))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        running(app) {
          val request = fakeRequest(GET, routes.VatController.showVatAccount().url)
          val result  = route(app, request).value

          status(result) mustBe OK

          if (DateUtils.isDayBefore20ThDayOfTheMonth(LocalDate.now())) {
            contentAsString(result) mustBe
              view(viewModel)(request, messages, appConfigForSetup).toString()
          }

          val doc = Jsoup.parse(contentAsString(result))

          Option(doc.getElementById("notification-panel")) should not be empty
        }
      }
  }

  "certificatesUnavailablePage" should {

    "render correctly" in {
      val view = instanceOf[import_vat_not_available](application)

      appConfig.historicStatementsEnabled = false

      val navigator = instanceOf[Navigator](application)

      val serviceUnavailableUrl: String =
        routes.ServiceUnavailableController.onPageLoad(navigator.importVatNotAvailablePageId).url

      running(application) {
        val request = fakeRequest(GET, routes.VatController.certificatesUnavailablePage().url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(Some(serviceUnavailableUrl))(request, messages, appConfig).toString()
      }
    }

    "display historic request url when feature is enabled" in {
      val app = applicationBuilder.build()

      val view = app.injector.instanceOf[import_vat_not_available]

      appConfig.historicStatementsEnabled = true

      val historicRequestUrl: String = appConfig.historicRequestUrl(C79Certificate)

      running(app) {
        val request = fakeRequest(GET, routes.VatController.certificatesUnavailablePage().url)
        val result  = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(Some(historicRequestUrl))(request, messages, appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector                   = mock[SdesConnector]

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory(EORI_NUMBER, None, None))
    val date: LocalDate               = LocalDate.now().withDayOfMonth(DAY_28)
    val navigator                     = new Navigator()

    when(mockFinancialsApiConnector.deleteNotification(any)(any))
      .thenReturn(Future.successful(true))

    val app: Application = applicationBuilder(eoriHistory)
      .overrides(
        inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
        inject.bind[SdesConnector].toInstance(mockSdesConnector),
        inject.bind[Navigator].toInstance(navigator)
      )
      .build()

    var appConfigForSetup: AppConfig = instanceOf[AppConfig](app)

    val view: import_vat = instanceOf[import_vat](app)
  }
}
