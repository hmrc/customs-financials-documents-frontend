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
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.mustBe

import play.api.i18n.Messages
import utils.CommonTestData.URL_TEST
import utils.SpecBase
import views.html.components.requestedStatements

class RequestedStatementsSpec extends SpecBase {

  "view" should {

    "display correct contents" when {

      "only url is provided" in new Setup {

        val viewDoc: Document = view(URL_TEST)

        shouldContainTheParagraphWithCorrectStyleClass(viewDoc)
        shouldContainTheCorrectLinkContents(
          viewDoc,
          linkMessageKey = "cf.security-statements.requested-link",
          preLinkMessage = "cf.account.detail.requested-certificates-available-text.pre",
          postLinkMessage = "cf.account.detail.requested-certificates-available-text.post"
        )
      }

      "all the arguments' value are provided" in new Setup {
        val viewDoc: Document = view(URL_TEST)

        shouldContainTheParagraphWithCorrectStyleClass(viewDoc)
        shouldContainTheCorrectLinkContents(
          viewDoc,
          linkMessageKey = "cf.postponed-vat.requested-statements-available-link-text",
          preLinkMessage = "cf.account.detail.requested-certificates-available-text.pre",
          postLinkMessage = "cf.account.detail.requested-certificates-available-text.post"
        )
      }
    }
  }

  private def shouldContainTheParagraphWithCorrectStyleClass(doc: Document): Assertion =
    doc.getElementsByClass("govuk-body govuk-!-margin-bottom-1") should not be empty

  private def shouldContainTheCorrectLinkContents(
    doc: Document,
    linkMessageKey: String,
    preLinkMessage: String,
    postLinkMessage: String
  )(implicit msgs: Messages): Assertion = {
    val docHtml = doc.html()

    docHtml.contains(URL_TEST) mustBe true
    docHtml.contains(messages(linkMessageKey)) mustBe true
    docHtml.contains(messages(preLinkMessage)) mustBe true
    docHtml.contains(messages(postLinkMessage)) mustBe true
  }

  trait Setup {

    protected def view(url: String): Document =
      Jsoup.parse(application.injector.instanceOf[requestedStatements].apply(url).body)
  }
}
