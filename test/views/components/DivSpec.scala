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
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.twirl.api.Html
import utils.SpecBase
import views.html.components.div
import play.api.Application

class DivSpec extends SpecBase with GuiceOneAppPerSuite {

  "div component" should {

    "display correct contents" when {

      "only content has been provided" in new Setup {
        val div: Elements = divComponent.select("div")

        div.text() mustBe contentText
        div.first().hasAttr("class") mustBe false
        div.first().hasAttr("id") mustBe false
      }

      "class has been provided along with content" in new Setup {
        val div: Elements = divComponentWithClass.select("div")

        div.text() mustBe contentText
        div.first().classNames() must contain(testClass)
        div.first().hasAttr("id") mustBe false
      }

      "id has been provided along with content" in new Setup {
        val div: Elements = divComponentWithId.select("div")

        div.text() mustBe contentText
        div.first().hasAttr("class") mustBe false
        div.first().attr("id") mustBe testId
      }

      "both class and id have been provided along with content" in new Setup {
        val div: Elements = divComponentWithClassAndId.select("div")

        div.text() mustBe contentText
        div.first().classNames() must contain(testClass)
        div.first().attr("id") mustBe testId
      }
    }
  }

  override def fakeApplication(): Application = applicationBuilder.build()

  trait Setup {
    val contentText   = "some content"
    val content: Html = Html(contentText)
    val testClass     = "test-class"
    val testId        = "test-id"

    val instanceOfDiv: div = instanceOf[div](app)

    val divComponent: Document               = Jsoup.parse(instanceOfDiv(content).body)
    val divComponentWithClass: Document      = Jsoup.parse(instanceOfDiv(content, classes = Some(testClass)).body)
    val divComponentWithId: Document         = Jsoup.parse(instanceOfDiv(content, id = Some(testId)).body)
    val divComponentWithClassAndId: Document =
      Jsoup.parse(instanceOfDiv(content, classes = Some(testClass), id = Some(testId)).body)
  }
}
