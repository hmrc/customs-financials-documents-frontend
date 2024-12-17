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

package views.components

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import play.api.Application
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import utils.SpecBase
import views.html.components.p

class PSpec extends SpecBase {

  "P component" should {

    "render the default class name when classes is not defined" in new SetUp {
      running(app) {
        val pView                         = app.injector.instanceOf[p]
        val output: HtmlFormat.Appendable = pView(
          message = "Hello, world!"
        )(messages(app))

        val html: Document = Jsoup.parse(contentAsString(output))

        html.getElementsByTag("p").attr("class") must include("govuk-body")
      }
    }

    "render the message and classes correctly without id, link, and tabLink" in new SetUp {
      running(app) {
        val pView                         = app.injector.instanceOf[p]
        val output: HtmlFormat.Appendable = pView(
          message = "Hello, world!",
          classes = "custom-class"
        )(messages(app))

        val html: Document = Jsoup.parse(contentAsString(output))

        html.getElementsByClass("custom-class").text() must include("Hello, world!")
      }
    }

    "render the message, classes, and id correctly with link and tabLink" in new SetUp {
      running(app) {
        val linkContent = Html("<a href='/link'>Link</a>")
        val tabContent  = Html("<a href='/tabLink'>Tab Link</a>")
        val pView       = app.injector.instanceOf[p]

        val output: HtmlFormat.Appendable = pView(
          message = "Hello, world!",
          classes = "custom-class",
          id = Some("test-id"),
          link = Some(linkContent),
          tabLink = Some(tabContent)
        )(messages(app))

        val html: Document = Jsoup.parse(contentAsString(output))

        html.getElementById("test-id").text() must include("Hello, world!")
        html.getElementById("test-id").select("a").get(0).text() mustBe "Link"
        html.getElementById("test-id").select("a").get(1).text() mustBe "Tab Link"
      }
    }
  }

  trait SetUp {
    val app: Application = application().build()
  }
}
