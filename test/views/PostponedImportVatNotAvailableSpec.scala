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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.postponed_import_vat_not_available

class PostponedImportVatNotAvailableSpec extends SpecBase with GuiceOneAppPerSuite {

  "PostponedImportVatNotAvailable view" should {

    "display correct title and guidance" in {
      import Setup.*

      view.title() mustBe
        s"${messages("cf.account.pvat.title")} - ${messages("service.name")} - GOV.UK"

      view.getElementById("no-statements").text() mustBe messages("cf.security-statements.unavailable")

      view.getElementById("missing-documents-guidance-heading").text() mustBe
        messages("cf.account.pvat.older-statements.heading")

      view.getElementById("pvat.support.message.heading").text() mustBe
        messages("cf.account.pvat.support.heading")

      view.getElementById("pvat.support.heading").text() must not be empty

      view.html().contains(tradeAndExciseEnquiryLink)
      view.getElementById("chief-guidance-heading").html() mustBe
        messages("cf.account.vat.chief.heading")
      view.html().contains(messages("cf.account.pvat.older-statements.description.3"))
      view.html().contains(serviceUnavailableUrl)
    }
  }

  override def fakeApplication(): Application = applicationBuilder.build()

  object Setup {
    val serviceUnavailableUrl: String = "service_unavailable_url"
    val eori                          = "test_eori"
    val hmrcDomainUrl                 = "https://www.gov.uk/government/organisations/hm-revenue-customs"
    val tradeAndExciseEnquiryLink     = s"$hmrcDomainUrl/contact/customs-international-trade-and-excise-enquiries"

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document = Jsoup.parse(
      instanceOf[postponed_import_vat_not_available](app)
        .apply(eori, Option(serviceUnavailableUrl))
        .body
    )
  }
}
