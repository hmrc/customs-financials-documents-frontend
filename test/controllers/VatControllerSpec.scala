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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import utils.{DateUtils, SpecBase}
import viewmodels.VatViewModel
import views.helpers.Formatters
import views.html.import_vat.{import_vat, import_vat_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class VatControllerSpec extends SpecBase {

  "showVatAccount" should {
    "redirect to certificates unavailable page if getting files fails" in new Setup {
      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad(navigator.importVatPageId).url
      val vatCertificateFile: VatCertificateFile = VatCertificateFile(
        "name_04",
        "download_url_06",
        111L,
        VatCertificateFileMetadata(
          date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          Pdf, C79Certificate, None), "")(messages(app))

      val currentCertificates = Seq(
        VatCertificatesByMonth(date.minusMonths(1), Seq(vatCertificateFile))(messages(app)),
        VatCertificatesByMonth(date.minusMonths(2), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(3), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(4), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(5), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(6), Seq())(messages(app)),
      )
      val vatCertificatesForEoris = Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))
      val viewModel: VatViewModel = VatViewModel(vatCertificatesForEoris)

      when(mockSdesConnector.getVatCertificates(any)(any, any))
        .thenReturn(Future.successful(Seq(vatCertificateFile)))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount.url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(viewModel, Some(serviceUnavailableUrl))(request, messages(app), appConfig).toString()
      }
    }

    "display the page correctly when files retrieved" in new Setup {
      when(mockSdesConnector.getVatCertificates(any)(any, any))
        .thenReturn(Future.failed(new RuntimeException("Something went wrong")))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount.url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.VatController.certificatesUnavailablePage().url
      }
    }

    "display the cert unavailable text for the relevant month when cert files are retrieved " +
      "after 14th of the month and cert is not available" in new Setup {

      val currentCertificates = Seq(
        VatCertificatesByMonth(date.minusMonths(1), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(2), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(3), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(4), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(5), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(6), Seq())(messages(app)),
      )
      val vatCertificatesForEoris = Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))
      val viewModel: VatViewModel = VatViewModel(vatCertificatesForEoris)

      when(mockSdesConnector.getVatCertificates(anyString)(any, any))
        .thenReturn(Future.successful(Seq()))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount.url)
        val result = route(app, request).value
        status(result) mustBe OK

        if (!DateUtils.isDayBefore15ThDayOfTheMonth(LocalDate.now())) {
          contentAsString(result) mustBe view(viewModel)(request, messages(app), appConfig).toString()
          val doc = Jsoup.parse(contentAsString(result))

          doc.getElementById("statements-list-0-row-5") should not be null
          doc.getElementById("statements-list-0-row-0").children().text() should include(messages(app)(
            "cf.account.vat.statements.unavailable", Formatters.dateAsMonth(date.minusMonths(1))(messages(app))))
        }
      }
    }

    "not display the cert row for the immediate previous month when cert files are retrieved " +
      "before 15th of the month and cert is not available for immediate previous month" in new Setup {

      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("import-vat").url
      val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
        VatCertificatesByMonth(date.minusMonths(2), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(3), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(4), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(5), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(6), Seq())(messages(app)),
      )
      val vatCertificatesForEoris: Seq[VatCertificatesForEori] = Seq(VatCertificatesForEori(eoriHistory.head,
        currentCertificates, Seq.empty))
      val viewModel: VatViewModel = VatViewModel(vatCertificatesForEoris)

      when(mockSdesConnector.getVatCertificates(anyString)(any, any))
        .thenReturn(Future.successful(Seq()))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount.url)
        val result = route(app, request).value
        status(result) mustBe OK

        if (DateUtils.isDayBefore15ThDayOfTheMonth(LocalDate.now())) {
          contentAsString(result) mustBe view(viewModel,
            Some(serviceUnavailableUrl))(request, messages(app), appConfig).toString()

          val doc = Jsoup.parse(contentAsString(result))

          doc.getElementById("statements-list-0-row-5") mustBe null
          doc.getElementById("statements-list-0-row-0") should not be null
          doc.getElementById("statements-list-0-row-1") should not be null
          doc.getElementById("statements-list-0-row-2") should not be null
          doc.getElementById("statements-list-0-row-3") should not be null
          doc.getElementById("statements-list-0-row-4") should not be null

          doc.getElementById("statements-list-0-row-0").children().text() should not include (messages(app)(
            "cf.account.vat.statements.unavailable", Formatters.dateAsMonth(date.minusMonths(1))(messages(app))))
        }
      }
    }

    "display all the certs' row when cert files are retrieved before 15th of the month and cert is available" in new Setup {
      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("import-vat").url
      val vatCertificateFile: VatCertificateFile = VatCertificateFile("name_04",
        "download_url_06",
        111L,
        VatCertificateFileMetadata(date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          Pdf,
          C79Certificate,
          None),
        "")(messages(app))

      val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
        VatCertificatesByMonth(date.minusMonths(1), Seq(vatCertificateFile))(messages(app)),
        VatCertificatesByMonth(date.minusMonths(2), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(3), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(4), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(5), Seq())(messages(app)),
        VatCertificatesByMonth(date.minusMonths(6), Seq())(messages(app)),
      )
      val vatCertificatesForEoris: Seq[VatCertificatesForEori] = Seq(VatCertificatesForEori(eoriHistory.head,
        currentCertificates, Seq.empty))
      val viewModel: VatViewModel = VatViewModel(vatCertificatesForEoris)

      when(mockSdesConnector.getVatCertificates(anyString)(any, any))
        .thenReturn(Future.successful(Seq(vatCertificateFile)))

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount.url)
        val result = route(app, request).value
        status(result) mustBe OK

        if (DateUtils.isDayBefore15ThDayOfTheMonth(LocalDate.now())) {
          contentAsString(result) mustBe
            view(viewModel, Some(serviceUnavailableUrl))(request, messages(app), appConfig).toString()

          val doc = Jsoup.parse(contentAsString(result))

          doc.getElementById("statements-list-0-row-0") should not be null
          doc.getElementById("statements-list-0-row-1") should not be null
          doc.getElementById("statements-list-0-row-2") should not be null
          doc.getElementById("statements-list-0-row-3") should not be null
          doc.getElementById("statements-list-0-row-4") should not be null
          doc.getElementById("statements-list-0-row-5") should not be null

          doc.getElementById("statements-list-0-row-0").children().text() should not include (messages(app)(
            "cf.account.vat.statements.unavailable", Formatters.dateAsMonth(date.minusMonths(1))(messages(app))))
        }
      }
    }
  }

  "certificatesUnavailablePage" should {
    "render correctly" in {
      val app = application().build()
      val view = app.injector.instanceOf[import_vat_not_available]
      val appConfig = app.injector.instanceOf[AppConfig]
      val navigator = app.injector.instanceOf[Navigator]

      val serviceUnavailableUrl: String =
        routes.ServiceUnavailableController.onPageLoad(navigator.importVatNotAvailablePageId).url

      running(app) {
        val request = fakeRequest(GET, routes.VatController.certificatesUnavailablePage().url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe
          view(Some(serviceUnavailableUrl))(request, messages(app), appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector = mock[SdesConnector]
    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))
    val date: LocalDate = LocalDate.now().withDayOfMonth(28)
    val navigator = new Navigator()

    when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
      .thenReturn(Future.successful(true))


    val app: Application = application(eoriHistory).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector),
      inject.bind[Navigator].toInstance(navigator)
    ).build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val view: import_vat = app.injector.instanceOf[import_vat]
  }
}
