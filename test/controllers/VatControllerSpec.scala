/*
 * Copyright 2022 HM Revenue & Customs
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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import utils.SpecBase
import viewmodels.VatViewModel
import views.html.import_vat.{import_vat, import_vat_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class VatControllerSpec extends SpecBase {

  "showVatAccount" should {
    "redirect to certificates unavailable page if getting files fails" in new Setup {
      val vatCertificateFile: VatCertificateFile = VatCertificateFile("name_04", "download_url_06", 111L, VatCertificateFileMetadata(date.minusMonths(1).getYear, date.minusMonths(1).getMonthValue, Pdf, C79Certificate, None), "")(messages(app))
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
        contentAsString(result) mustBe view(viewModel)(request, messages(app), appConfig).toString()
      }
    }

    "display the page correctly when files retrieved" in new Setup {
      when(mockSdesConnector.getVatCertificates(any)(any, any))
        .thenReturn(Future.failed(new RuntimeException("Something went wrong")))

      running(app) {
        val request = fakeRequest(GET, routes.VatController.showVatAccount.url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.VatController.certificatesUnavailablePage.url
      }
    }
  }

  "certificatesUnavailablePage" should {
    "render correctly" in {
      val app = application().build()
      val view = app.injector.instanceOf[import_vat_not_available]
      val appConfig = app.injector.instanceOf[AppConfig]

      running(app){
        val request = fakeRequest(GET, routes.VatController.certificatesUnavailablePage.url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view()(request, messages(app), appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector = mock[SdesConnector]
    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))
    val date: LocalDate = LocalDate.now().withDayOfMonth(28)

    when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
      .thenReturn(Future.successful(true))


    val app: Application = application(eoriHistory).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector)
    ).build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val view: import_vat = app.injector.instanceOf[import_vat]
  }
}
