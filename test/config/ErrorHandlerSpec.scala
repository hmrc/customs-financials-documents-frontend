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

package config

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

class ErrorHandlerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val fakeRequest = FakeRequest("GET", "/")

  private val handler               = app.injector.instanceOf[ErrorHandler]
  implicit val ec: ExecutionContext = ExecutionContext.global

  "Error handler" should {

    "render standard error template HTML" in {
      val response =
        handler.standardErrorTemplate(pageTitle = "title", heading = "heading", message = "message")(fakeRequest)

      response.map { html =>
        html.contentType shouldBe "text/html"
      }
    }

    "render not found template HTML" in {
      val response = handler.notFoundTemplate(fakeRequest)

      response.map { html =>
        html.contentType shouldBe "text/html"
      }
    }

    "render unauthorized HTML" in {
      val html = handler.unauthorized()(fakeRequest)
      html.contentType shouldBe "text/html"
    }
  }
}
