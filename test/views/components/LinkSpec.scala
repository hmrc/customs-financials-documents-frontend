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
import play.api.i18n.Messages
import utils.SpecBase
import views.html.components.link

class LinkSpec extends SpecBase {

  "link component" should {

    "display correct contents" when {

      "only link message and location have been provided" in new Setup {
        linkComponent.text() mustBe messages(linkMessage)

        val link: Elements = linkComponent.select("a")

        link.size() mustBe 1
        link.first().hasAttr("class") mustBe true
        link.first().classNames() must contain(defaultClass)
        link.first().attr("href") mustBe location
        link.first().hasAttr("id") mustBe false
      }

      "class has been provided along with link message and location" in new Setup {
        linkComponentWithClass.text() mustBe messages(linkMessage)

        val link: Elements = linkComponentWithClass.select("a")

        link.first().classNames() must contain(classes)
      }

      "id has been provided along with link message and location" in new Setup {
        linkComponentWithId.text() mustBe messages(linkMessage)

        val link: Elements = linkComponentWithId.select("a")

        link.first().attr("id") mustBe linkId
      }

      "classes and id have been provided along with link message and location" in new Setup {
        linkComponentWithClassAndId.text() mustBe messages(linkMessage)

        val link: Elements = linkComponentWithClassAndId.select("a")

        link.first().classNames() must contain(classes)
        link.first().attr("id") mustBe linkId
      }

      "aria-label has been provided along with link message and location" in new Setup {
        linkComponentWithAriaLabel.text() must include(messages(linkMessage))
        linkComponentWithAriaLabel.text() must include(ariaLabelText)

        val link: Elements = linkComponentWithAriaLabel.select("a")

        link.first().select(".govuk-visually-hidden").text() mustBe ariaLabelText
      }
    }
  }

  trait Setup {
    val linkMessage   = "linkMessage"
    val location      = "jackie-chan.com"
    val defaultClass  = "govuk-link"
    val classes       = "custom-class"
    val linkId        = "custom-id"
    val ariaLabelText = "visually hidden label"

    val instanceOfLink: link = instanceOf[link](application)

    val linkComponent: Document          = Jsoup.parse(instanceOfLink(linkMessage, location).body)
    val linkComponentWithClass: Document = Jsoup.parse(instanceOfLink(linkMessage, location, linkClass = classes).body)
    val linkComponentWithId: Document    = Jsoup.parse(instanceOfLink(linkMessage, location, linkId = Some(linkId)).body)

    val linkComponentWithClassAndId: Document =
      Jsoup.parse(instanceOfLink(linkMessage, location, linkId = Some(linkId), linkClass = classes).body)

    val linkComponentWithAriaLabel: Document =
      Jsoup.parse(instanceOfLink(linkMessage, location, ariaLabel = Some(ariaLabelText)).body)
  }
}
