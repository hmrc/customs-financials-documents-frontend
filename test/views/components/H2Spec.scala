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
import views.html.components.h2

class H2Spec extends SpecBase {

  "h2 component" should {

    "display correct contents" when {

      "only message key has been provided" in new Setup {
        h2Component.text() mustBe message(msgKey)

        val h2: Elements = h2Component.select("h2")

        h2.size() mustBe 1
        h2.first().hasAttr("class") mustBe true
        h2.first().classNames() must contain(defaultClass)
        h2.first().hasAttr("id") mustBe false
      }

      "class has been provided along with message key" in new Setup {
        h2ComponentWithClass.text() mustBe message(msgKey)

        val h2: Elements = h2ComponentWithClass.select("h2")

        h2.first().classNames() must contain(classes)
      }

      "id has been provided along with message key" in new Setup {
        h2ComponentWithId.text() mustBe message(msgKey)

        val h2: Elements = h2ComponentWithId.select("h2")

        h2.first().attr("id") mustBe id
      }

      "classes and id have been provided along with message key" in new Setup {
        h2ComponentWithClassAndId.text() mustBe message(msgKey)

        val h2: Elements = h2ComponentWithClassAndId.select("h2")

        h2.first().classNames() must contain(classes)
        h2.first().attr("id") mustBe id
      }
    }
  }

  trait Setup {
    val msgKey = "messageKey"
    val defaultClass = "govuk-heading-m"
    val classes = "custom-class"
    val id = "custom-id"

    val app: Application = application().build()
    implicit val message: Messages = messages(app)
    val instanceOfH2: h2 = app.injector.instanceOf[h2]

    val h2Component: Document = Jsoup.parse(instanceOfH2(msgKey).body)
    val h2ComponentWithClass: Document = Jsoup.parse(instanceOfH2(msgKey, classes = classes).body)
    val h2ComponentWithId: Document = Jsoup.parse(instanceOfH2(msgKey, id = Some(id)).body)
    val h2ComponentWithClassAndId: Document = Jsoup.parse(instanceOfH2(msgKey, id = Some(id), classes = classes).body)
  }
}
