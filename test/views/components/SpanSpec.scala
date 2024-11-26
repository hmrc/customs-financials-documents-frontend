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
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import views.html.components.span

class SpanSpec extends SpecBase {

  "span component" should {

    "display correct contents" when {

      "only message key has been provided" in new Setup {
        spanComponent.text() mustBe message(msgKey)

        val span: Elements = spanComponent.select("span")

        span.size() mustBe 1
        span.first().hasAttr("class") mustBe false
        span.first().hasAttr("aria-hidden") mustBe false
      }

      "class has been provided along with message key" in new Setup {
        spanComponentWithClass.text() mustBe message(msgKey)

        val span: Elements = spanComponentWithClass.select("span")

        span.size() mustBe 1
        span.first().classNames() must contain(classes)
      }

      "ariaHidden has been provided along with message key" in new Setup {
        spanComponentWithAriaHidden.text() mustBe message(msgKey)

        val span: Elements = spanComponentWithAriaHidden.select("span")

        span.size() mustBe 1
        span.first().attr("aria-hidden") mustBe ariaHidden
      }

      "classes and ariaHidden have been provided along with message key" in new Setup {
        spanComponentWithClassAndAriaHidden.text() mustBe message(msgKey)

        val span: Elements = spanComponentWithClassAndAriaHidden.select("span")

        span.size() mustBe 1
        span.first().classNames() must contain(classes)
        span.first().attr("aria-hidden") mustBe ariaHidden
      }
    }
  }

  trait Setup {
    val msgKey = "messageKey"
    val classes = "custom-class"
    val ariaHidden = "true"

    val app: Application = application().build()
    implicit val message: Messages = messages(app)
    val instanceOfSpan: span = app.injector.instanceOf[span]

    val spanComponent: Document = Jsoup.parse(instanceOfSpan(msgKey).body)
    val spanComponentWithClass: Document = Jsoup.parse(instanceOfSpan(msgKey, classes = Some(classes)).body)
    val spanComponentWithAriaHidden: Document = Jsoup.parse(instanceOfSpan(msgKey, ariaHidden = Some(ariaHidden)).body)

    val spanComponentWithClassAndAriaHidden: Document =
      Jsoup.parse(instanceOfSpan(msgKey, Some(classes), Some(ariaHidden)).body)
  }
}
