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

import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class Navigator @Inject()() {

  val importVatPageId = "import-vat"
  val importVatNotAvailablePageId = "import-vat-not-available"
  val postponedVatPageId = "postponed-vat"
  val postponedVatNotAvailablePageId = "postponed-vat-not-available"

  def backLinkUrlForServiceUnavailablePage(id: String): Option[String] =
    id match {
      case pageId if pageId == importVatPageId => Some(controllers.routes.VatController.showVatAccount.url)

      case pageId if pageId == importVatNotAvailablePageId =>
        Some(controllers.routes.VatController.certificatesUnavailablePage().url)

      case pageId if pageId == postponedVatPageId =>
        Some(controllers.routes.PostponedVatController.show(Some("CDS")).url)

      case pageId if pageId == postponedVatNotAvailablePageId =>
        Some(controllers.routes.PostponedVatController.statementsUnavailablePage().url)

      case _ => None
    }
}
