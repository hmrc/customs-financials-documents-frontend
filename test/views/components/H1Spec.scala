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
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import views.html.components.h1

class H1Spec extends SpecBase with GuiceOneAppPerSuite {

  import Setup.*

  "h1 component" should {

    "display correct contents" when {

      "only message key has been provided" in {
        h1Component.text() mustBe messages(msgKey)

        val h1: Elements = h1Component.select("h1")

        h1.size() mustBe 1
        h1.first().hasAttr("class") mustBe true
        h1.first().classNames() must contain(defaultClass)
        h1.first().hasAttr("id") mustBe false
      }

      "class has been provided along with message key" in {
        h1ComponentWithClass.text() mustBe messages(msgKey)

        val h1: Elements = h1ComponentWithClass.select("h1")

        h1.first().classNames() must contain(classes)
      }

      "id has been provided along with message key" in {
        h1ComponentWithId.text() mustBe messages(msgKey)

        val h1: Elements = h1ComponentWithId.select("h1")

        h1.first().attr("id") mustBe id
      }

      "classes and id have been provided along with message key" in {
        h1ComponentWithClassAndId.text() mustBe messages(msgKey)

        val h1: Elements = h1ComponentWithClassAndId.select("h1")

        h1.first().classNames() must contain(classes)
        h1.first().attr("id") mustBe id
      }
    }
  }

  override def fakeApplication(): Application = applicationBuilder.build()

  object Setup {
    val msgKey       = "messageKey"
    val defaultClass = "govuk-heading-l"
    val classes      = "custom-class"
    val id           = "custom-id"

    val instanceOfH1: h1 = instanceOf[h1](app)

    val h1Component: Document               = Jsoup.parse(instanceOfH1(msgKey).body)
    val h1ComponentWithClass: Document      = Jsoup.parse(instanceOfH1(msgKey, classes = classes).body)
    val h1ComponentWithId: Document         = Jsoup.parse(instanceOfH1(msgKey, id = Some(id)).body)
    val h1ComponentWithClassAndId: Document = Jsoup.parse(instanceOfH1(msgKey, id = Some(id), classes = classes).body)
  }
}
