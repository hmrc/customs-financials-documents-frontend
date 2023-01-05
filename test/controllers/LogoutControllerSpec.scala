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

import java.net.URLEncoder

class LogoutControllerSpec extends SpecBase {

  "logout" should {
    "redirect to logout link with survey continue" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.LogoutController.logout.url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe s"${config.signOutUrl}?continue=${URLEncoder.encode(config.feedbackService, "UTF-8")}"
      }
    }
  }
  "logoutNoSurvey" should {
    "redirect to logout link without survey continue" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.LogoutController.logoutNoSurvey.url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe s"${config.signOutUrl}?continue=${URLEncoder.encode(config.loginContinueUrl, "UTF-8")}"
      }
    }
  }

  trait Setup {
    val app: Application = application().build()
    val config: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
