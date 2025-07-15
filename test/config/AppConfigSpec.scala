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
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import play.api.test.Helpers.running

class AppConfigSpec extends SpecBase with GuiceOneAppPerSuite {

  "FrontendAppConfig" should {

    "include the app name" in {
      appConfig.appName mustBe "customs-financials-documents-frontend"
    }

    "contain the correct GOV survey banner URL" in {
      appConfig.helpMakeGovUkBetterUrl shouldBe
        "https://survey.take-part-in-research.service.gov.uk/jfe/form/SV_74GjifgnGv6GsMC?Source=BannerList_HMRC_CDS_MIDVA"
    }

    "historic request url" should {
      "contain 'adjustments'" in {
        appConfig.historicRequestUrl(FileRole.SecurityStatement) must include("adjustments")
      }

      "contain 'import-vat'" in {
        appConfig.historicRequestUrl(FileRole.C79Certificate) must include("import-vat")
      }

      "return empty string for invalid filerole" in {
        appConfig.historicRequestUrl(FileRole.PostponedVATAmendedStatement) mustBe empty
      }
    }

    "requested statements url" should {
      "contain adjustments" in {
        appConfig.requestedStatements(FileRole.SecurityStatement) must include("adjustments")
      }

      "contain import-vat" in {
        appConfig.requestedStatements(FileRole.C79Certificate) must include("import-vat")
      }

      "return empty string for invalid filerole" in {
        appConfig.requestedStatements(FileRole.PostponedVATAmendedStatement) mustBe empty
      }
    }

    "contain correct subscribeCdsUrl" in {
      appConfig.subscribeCdsUrl mustBe "https://www.tax.service.gov.uk/customs-enrolment-services/cds/subscribe"
    }

    "contain correct contactFrontEndServiceId" in {
      appConfig.contactFrontEndServiceId mustBe "CDS Financials"
    }

    "return correct value for deskProLinkUrlForServiceUnavailable" in {
      val path                                                     = "test_Path"
      implicit val reqHeaders: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", path)

      appConfig.deskProLinkUrlForServiceUnavailable mustBe
        "http://localhost:9250" +
        "/contact/report-technical-problem?newTab=true&amp;service=CDS%20FinancialsreferrerUrl=test_Path"
    }

    "contain the correct feedbackService url" in {
      appConfig.feedbackService mustBe "http://localhost:9514/feedback/CDS-FIN"
    }
  }

  "emailFrontendService" should {
    "return the correct service address with context" in {
      appConfig.emailFrontendService mustBe "http://localhost:9898/manage-email-cds"
    }
  }

  "emailFrontendUrl" should {
    "return the correct url" in {
      appConfig.emailFrontendUrl mustBe "http://localhost:9898/manage-email-cds/service/customs-finance"
    }
  }
}
