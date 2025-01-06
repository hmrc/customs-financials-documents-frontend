/*
 * Copyright 2024 HM Revenue & Customs
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

package views.components.description_list

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.SpecBase
import views.html.components.description_list.{dd, dl, dt}

class DescriptionListSpec extends SpecBase {

  "dd component" should {

    "display correct contents" when {

      "only content has been provided" in new Setup {
        val dd: Elements = ddComponent.select("dd")

        dd.text() mustBe contentText
        dd.first().hasAttr("class") mustBe false
        dd.first().hasAttr("id") mustBe false
      }

      "class has been provided along with content" in new Setup {
        val dd: Elements = ddComponentWithClass.select("dd")

        dd.text() mustBe contentText
        dd.first().classNames() must contain(testClass)
        dd.first().classNames() must contain(testClass)
        dd.first().hasAttr("id") mustBe false
      }

      "id has been provided along with content" in new Setup {
        val dd: Elements = ddComponentWithId.select("dd")

        dd.text() mustBe contentText
        dd.first().hasAttr("class") mustBe false
        dd.first().attr("id") mustBe testId
      }

      "both class and id have been provided along with content" in new Setup {
        val dd: Elements = ddComponentWithClassAndId.select("dd")

        dd.text() mustBe contentText
        dd.first().classNames() must contain(testClass)
        dd.first().attr("id") mustBe testId
      }
    }
  }

  "dl component" should {

    "display correct contents" when {

      "only content has been provided" in new Setup {
        val dl: Elements = dlComponent.select("dl")

        dl.html() mustBe contentText
        dl.first().hasAttr("class") mustBe false
        dl.first().hasAttr("id") mustBe false
      }

      "class has been provided along with content" in new Setup {
        val dl: Elements = dlComponentWithClass.select("dl")

        dl.html() mustBe contentText
        dl.first().classNames() must contain(testClass)
        dl.first().classNames() must contain(testClass1)
        dl.first().hasAttr("id") mustBe false
      }

      "id has been provided along with content" in new Setup {
        val dl: Elements = dlComponentWithId.select("dl")

        dl.html() mustBe contentText
        dl.first().hasAttr("class") mustBe false
        dl.first().attr("id") mustBe testId
      }

      "both class and id have been provided along with content" in new Setup {
        val dl: Elements = dlComponentWithClassAndId.select("dl")

        dl.html() mustBe contentText
        dl.first().classNames() must contain(testClass)
        dl.first().attr("id") mustBe testId
      }
    }
  }

  "dt component" should {

    "display correct contents" when {

      "only content has been provided" in new Setup {
        val dt: Elements = dtComponent.select("dt")

        dt.text() mustBe contentText
        dt.first().hasAttr("class") mustBe false
        dt.first().hasAttr("id") mustBe false
      }

      "class has been provided along with content" in new Setup {
        val dt: Elements = dtComponentWithClass.select("dt")

        dt.text() mustBe contentText
        dt.first().classNames() must contain(testClass)
        dt.first().hasAttr("id") mustBe false
      }

      "id has been provided along with content" in new Setup {
        val dt: Elements = dtComponentWithId.select("dt")

        dt.text() mustBe contentText
        dt.first().hasAttr("class") mustBe false
        dt.first().attr("id") mustBe testId
      }

      "both class and id have been provided along with content" in new Setup {
        val dt: Elements = dtComponentWithClassAndId.select("dt")

        dt.text() mustBe contentText
        dt.first().classNames() must contain(testClass)
        dt.first().attr("id") mustBe testId
      }
    }
  }

  trait Setup {
    val contentText          = "some content"
    val content: Html        = Html(contentText)
    val testClass            = "test-class"
    val testClass1           = "test-new1"
    val testMultipleClassses = "test-class test-new1 test-new2"
    val testId               = "test-id"

    val app: Application           = application().build()
    implicit val message: Messages = messages(app)

    val instanceOfDd: dd = app.injector.instanceOf[dd]
    val instanceOfDl: dl = app.injector.instanceOf[dl]
    val instanceOfDt: dt = app.injector.instanceOf[dt]

    val ddComponent: Document               = Jsoup.parse(instanceOfDd(content).body)
    val ddComponentWithClass: Document      = Jsoup.parse(instanceOfDd(content, classes = Some(testClass)).body)
    val ddComponentWithId: Document         = Jsoup.parse(instanceOfDd(content, id = Some(testId)).body)
    val ddComponentWithClassAndId: Document =
      Jsoup.parse(instanceOfDd(content, classes = Some(testClass), id = Some(testId)).body)

    val dlComponent: Document               = Jsoup.parse(instanceOfDl(content).body)
    val dlComponentWithClass: Document      = Jsoup.parse(instanceOfDl(content, classes = Some(testMultipleClassses)).body)
    val dlComponentWithId: Document         = Jsoup.parse(instanceOfDl(content, id = Some(testId)).body)
    val dlComponentWithClassAndId: Document =
      Jsoup.parse(instanceOfDl(content, classes = Some(testClass), id = Some(testId)).body)

    val dtComponent: Document               = Jsoup.parse(instanceOfDt(content).body)
    val dtComponentWithClass: Document      = Jsoup.parse(instanceOfDt(content, classes = Some(testClass)).body)
    val dtComponentWithId: Document         = Jsoup.parse(instanceOfDt(content, id = Some(testId)).body)
    val dtComponentWithClassAndId: Document =
      Jsoup.parse(instanceOfDt(content, classes = Some(testClass), id = Some(testId)).body)
  }
}
