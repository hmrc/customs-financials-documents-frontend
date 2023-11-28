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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import play.api.test.Helpers._
import utils.SpecBase
import models.EmailUnverifiedResponse
import views.html.email.{undeliverable_email, verify_your_email}

class EmailControllerSpec extends SpecBase {

  // "showUnverified" should {
  //   "display the unverified page" in new Setup {
  //     running(app) {
  //       val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
  //       val result = route(app, request).value
  //       status(result) mustBe OK
  //       contentAsString(result) mustBe verifyYourEmailView(config.emailFrontendUrl, Some("unverifiedEmail"))(request, messages(app), config).toString()
  //     }
  //   }
  // }

  "showUndeliverable" should {
    "display the undeliverable page" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe undeliverableEmailView(config.emailFrontendUrl)(request, messages(app), config).toString()
      }
    }
  }

  trait Setup {
    val app: Application = application().build()
    val config: AppConfig = app.injector.instanceOf[AppConfig]
    val verifyYourEmailView: verify_your_email = app.injector.instanceOf[verify_your_email]
    val undeliverableEmailView: undeliverable_email = app.injector.instanceOf[undeliverable_email]
  }
}
