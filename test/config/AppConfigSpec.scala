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

package config

import models.FileRole
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.Application
import utils.SpecBase
import play.api.test.Helpers.running

class AppConfigSpec extends SpecBase {

  "FrontendAppConfig" should {

    "include the app name" in new Setup {
      running(app) {
        appConfig.appName mustBe "customs-financials-documents-frontend"
      }
    }

    "historic request url" should {
      "contain 'adjustments'" in new Setup {
        running(app) {
          appConfig.historicRequestUrl(FileRole.SecurityStatement) must include ("adjustments")
        }
      }

      "contain 'import-vat'" in new Setup {
        running(app) {
          appConfig.historicRequestUrl(FileRole.C79Certificate) must include ("import-vat")
        }
      }

      "return empty string for invalid filerole" in new Setup {
        running(app) {
          appConfig.historicRequestUrl(FileRole.PostponedVATAmendedStatement) mustBe ("")
        }
      }
    }

    "requested statements url" should {
      "contain adjustments" in new Setup {
        running(app) {
          appConfig.requestedStatements(FileRole.SecurityStatement) must include ("adjustments")
        }
      }

      "contain import-vat" in new Setup {
        running(app) {
          appConfig.requestedStatements(FileRole.C79Certificate) must include ("import-vat")
        }
      }

      "return empty string for invalid filerole" in new Setup {
        running(app) {
          appConfig.requestedStatements(FileRole.PostponedVATAmendedStatement) mustBe ("")
        }
      }
    }

    "contain correct subscribeCdsUrl" in new Setup {
      appConfig.subscribeCdsUrl mustBe
        "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"
    }
  }

  trait Setup {
    val app: Application = application(allEoriHistory = Seq.empty).build()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
