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
import connectors.DataStoreConnector
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.OK
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.SpecBase
import views.html.request_service_unavailable

import scala.concurrent.Future

class RequestServiceUnavailableControllerSpec extends SpecBase {
  "requestServiceUnavailablePage" should {
    "render the historic request service unavailable page" in new Setup {
      when(mockDataStoreConnector.getEmail(any)(any))
        .thenReturn(Future.successful(Right(Email("some@email.com"))))

      running(app) {
        val request = fakeRequest(GET, routes.RequestServiceUnavailableController.requestServiceUnavailablePage().url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view()(request, messages(app), appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockDataStoreConnector: DataStoreConnector = mock[DataStoreConnector]
    val app: Application = application().overrides(
      inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
    ).build()
    val view: request_service_unavailable = app.injector.instanceOf[request_service_unavailable]
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }

}
