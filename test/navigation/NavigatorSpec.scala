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

package navigation

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import utils.SpecBase

class NavigatorSpec extends SpecBase {
  "backLinkUrlForServiceUnavailablePage" should {
    "return correct url for ids " in new Setup {
      navigatorOb.backLinkUrlForServiceUnavailablePage(navigatorOb.importVatPageId) mustBe
        Some(controllers.routes.VatController.showVatAccount.url)

      navigatorOb.backLinkUrlForServiceUnavailablePage(navigatorOb.postponedVatPageId) mustBe
        Some(controllers.routes.PostponedVatController.show(Some("CDS")).url)

      navigatorOb.backLinkUrlForServiceUnavailablePage("Some_Id") mustBe empty
    }
  }

  trait Setup {
    val navigatorOb = new Navigator()
  }
}
