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
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import utils.SpecBase
import utils.Utils.emptyString
import views.html.not_subscribed_to_cds

import java.net.URLEncoder
import scala.concurrent.{ExecutionContext, Future}

class UnauthorisedControllerSpec extends SpecBase {

  "onPageLoad" should {

    "render the not subscribed to CDS page" in new Setup {
      when(mockAuthConnector.authorise(
        eqTo(AuthProviders(GovernmentGateway)), eqTo(EmptyRetrieval))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful({}))

      running(app) {
        val request = fakeRequest(GET, routes.UnauthorisedController.onPageLoad.url)
        val result = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe view()(request, messages(app), appConfig).toString()
      }
    }

    "redirect to login if no auth session found" in new Setup {
      when(mockAuthConnector.authorise(
        eqTo(AuthProviders(GovernmentGateway)), eqTo(EmptyRetrieval))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new UnauthorizedException(emptyString)))

      running(app) {
        val request = fakeRequest(GET, routes.UnauthorisedController.onPageLoad.url)

        val result = route(app, request).value
        redirectLocation(result).value mustBe
          s"${appConfig.loginUrl}?continue_url=${URLEncoder.encode(appConfig.loginContinueUrl, "UTF-8")}"
      }
    }
  }

  trait Setup {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val app: Application = application().overrides(
      inject.bind[AuthConnector].toInstance(mockAuthConnector)
    ).build()

    val view: not_subscribed_to_cds = app.injector.instanceOf[not_subscribed_to_cds]
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
