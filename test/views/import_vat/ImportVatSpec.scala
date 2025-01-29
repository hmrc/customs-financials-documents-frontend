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

package views.import_vat

import config.AppConfig
import models.EoriHistory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import viewmodels.ImportVatViewModel
import views.html.import_vat.import_vat
import models.{VatCertificatesByMonth, VatCertificatesForEori}
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import utils.CommonTestData.{
  DAY_28, EORI_NUMBER, FIVE_MONTHS, FOUR_MONTHS, ONE_MONTH, SIX_MONTHS, THREE_MONTHS, TWO_MONTHS
}
import utils.Utils.emptyString

import java.time.LocalDate

class ImportVatSpec extends SpecBase with GuiceOneAppPerSuite {

  "ImportVat view" should {

    "display the correct title and guidance" in {
      import Setup.*

      view.title() mustBe
        s"${messages("cf.account.vat.title")} - ${messages("service.name")} - GOV.UK"

      view.getElementById("import-vat-certificates-heading").text() mustBe messages("cf.account.vat.title")
      view.getElementById("missing-certificates-guidance-heading").text() mustBe
        messages("cf.account.vat.older-certificates.heading")
      view.getElementById("chief-guidance-heading").text() mustBe
        messages("cf.account.vat.chief.heading")
      view.getElementById("vat.support.message.heading").text() mustBe
        messages("cf.account.vat.support.heading")

      view.html().contains("cf.account.vat.available-text")
      view.html().contains("cf.account.vat.your-certificates.heading")
      view.html().contains("cf.account.vat.support.link")
      view.html().contains("cf.account.vat.older-certificates.description.2")
      view.html().contains("cf.account.vat.older-certificates.description.link")
      view.html().contains("cf.account.vat.older-certificates.description.1")
      view.html().contains(appConfig.c79EmailAddressHref)
      view.html().contains(appConfig.c79EmailAddress)
      view.html().contains(serviceUnavailableUrl.getOrElse(emptyString))
    }
  }

  override def fakeApplication(): Application = applicationBuilder.build()

  object Setup {
    val serviceUnavailableUrl: Option[String] = Option("service_unavailable_url")

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory(EORI_NUMBER, None, None))
    val date: LocalDate               = LocalDate.now().withDayOfMonth(DAY_28)

    val currentCertificates: Seq[VatCertificatesByMonth] = Seq(
      VatCertificatesByMonth(date.minusMonths(ONE_MONTH), Seq())(messages),
      VatCertificatesByMonth(date.minusMonths(TWO_MONTHS), Seq())(messages),
      VatCertificatesByMonth(date.minusMonths(THREE_MONTHS), Seq())(messages),
      VatCertificatesByMonth(date.minusMonths(FOUR_MONTHS), Seq())(messages),
      VatCertificatesByMonth(date.minusMonths(FIVE_MONTHS), Seq())(messages),
      VatCertificatesByMonth(date.minusMonths(SIX_MONTHS), Seq())(messages)
    )

    val vatCertificatesForEoris: Seq[VatCertificatesForEori] =
      Seq(VatCertificatesForEori(eoriHistory.head, currentCertificates, Seq.empty))

    val viewModel: ImportVatViewModel = ImportVatViewModel(vatCertificatesForEoris, serviceUnavailableUrl)

    val view: Document = Jsoup.parse(instanceOf[import_vat](app).apply(viewModel).body)
  }
}
