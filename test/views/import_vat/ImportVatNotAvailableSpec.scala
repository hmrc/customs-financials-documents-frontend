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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.import_vat.import_vat_not_available

class ImportVatNotAvailableSpec extends SpecBase {

  "ImportVatNotAvailable view" should {

    "display correct title and guidance" in new Setup {
      view.title() mustBe
        s"${messages(app)("cf.account.vat.title")} - ${messages(app)("service.name")} - GOV.UK"

      view.getElementById("missing-certificates-guidance-heading").html() mustBe
        messages(app)("cf.account.vat.older-certificates.heading")

      view.getElementById("chief-guidance-heading").html() mustBe
        messages(app)("cf.account.vat.chief.heading")

      view.getElementById("vat.support.message.heading").html() mustBe
        messages(app)("cf.account.vat.support.heading")

      view.html().contains(messages(app)("cf.account.vat.older-certificates.description.2"))
      view.html().contains(messages(app)("cf.account.vat.support.link"))
      view.html().contains(serviceUnavailableUrl)
    }
  }

  trait Setup {
    val app: Application = application().build()
    val serviceUnavailableUrl: String = "service_unavailable_url"

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[import_vat_not_available].apply(Option(serviceUnavailableUrl)).body)
  }
}
