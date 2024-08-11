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

package views.postponed_vat

import models.PostponedVatStatementGroup
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.postponed_vat.current_statement_row

class CurrentStatementRowSpec extends SpecBase {

  "view" should {

    "display correct contents" when {

      "statementGroup has statements" in {

      }

      "statementGroup has no statements but start date is of previous month and after 19th" in {

      }

      "statementGroup has no statements but start date is of current month" in {

      }

      "statementGroup has statements and start date is of previous month and after 19th" in {

      }
    }
  }

  trait Setup {
    val app: Application = application().build()

    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    def view(statementGroup: PostponedVatStatementGroup,
             dutyPaymentMethodSource: Seq[String],
             isCdsOnly: Boolean): Document =
      Jsoup.parse(app.injector.instanceOf[current_statement_row].apply(
        statementGroup,
        dutyPaymentMethodSource,
        isCdsOnly).body)
  }
}
