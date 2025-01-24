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

import navigation.Navigator
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.Application
import play.api.http.Status.OK
import play.api.test.Helpers.{
  GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty
}
import play.api.test.Helpers._
import utils.SpecBase
import views.html.service_unavailable

class ServiceUnavailableControllerSpec extends SpecBase {

  "onPageLoad" should {

    "render service unavailable page" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.ServiceUnavailableController.onPageLoad("id-not-defined").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe view()(request, messages, appConfig).toString()
      }
    }

    "render service unavailable page for PVAT statements page" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.ServiceUnavailableController.onPageLoad("postponed-vat").url)
        val result  = route(app, request).value

        status(result) mustBe OK

        val backlink = Some(routes.PostponedVatController.show(Some("CDS")).url)
        contentAsString(result) mustBe view(backlink)(request, messages, appConfig).toString()
      }
    }

    "render service unavailable page for C79 (Import VAT) statements page" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.ServiceUnavailableController.onPageLoad("import-vat").url)
        val result  = route(app, request).value

        val backlink = Some(routes.VatController.showVatAccount().url)

        status(result) mustBe OK
        contentAsString(result) mustBe view(backlink)(request, messages, appConfig).toString()
      }
    }
  }

  trait Setup {
    val app: Application          = applicationBuilder.build()
    val view: service_unavailable = app.injector.instanceOf[service_unavailable]

    val navigator: Navigator = new Navigator()
  }
}
