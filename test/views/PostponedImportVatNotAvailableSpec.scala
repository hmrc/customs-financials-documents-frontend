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

package views

import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.postponed_import_vat_not_available

class PostponedImportVatNotAvailableSpec extends SpecBase {

  "PostponedImportVatNotAvailable view" should {

    "display correct title and guidance" in new Setup {
      view.title() mustBe
        s"${messages(app)("cf.account.pvat.title")} - ${messages(app)("service.name")} - GOV.UK"

      view.getElementById("no-statements").text() mustBe messages(app)("cf.security-statements.unavailable")

      view.getElementById("missing-documents-guidance-heading").text() mustBe
        messages(app)("cf.account.pvat.older-statements.heading")

      view.getElementById("pvat.support.message.heading").text() mustBe
        messages(app)("cf.account.pvat.support.heading")

      view.getElementById("pvat.support.heading").text() must not be empty

      view.html().contains(tradeAndExciseEnquiryLink)
      view.getElementById("chief-guidance-heading").html() mustBe
        messages(app)("cf.account.vat.chief.heading")
      view.html().contains(messages(app)("cf.account.pvat.older-statements.description.3"))
      view.html().contains(serviceUnavailableUrl)
    }
  }

  trait Setup {
    val app: Application              = application().build()
    val serviceUnavailableUrl: String = "service_unavailable_url"
    val eori                          = "test_eori"
    val hmrcDomainUrl                 = "https://www.gov.uk/government/organisations/hm-revenue-customs"
    val tradeAndExciseEnquiryLink     = s"$hmrcDomainUrl/contact/customs-international-trade-and-excise-enquiries"

    implicit val appConfig: AppConfig                         = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages                                = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[postponed_import_vat_not_available].apply(eori, Option(serviceUnavailableUrl)).body
    )
  }
}
