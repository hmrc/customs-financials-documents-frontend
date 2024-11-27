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

package utils

import org.scalatest.matchers.must.Matchers.mustBe
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.Utils._
import views.html.components.description_list.{dd, dl, dt}
import views.html.components.{div, h1, h2, inset, link, p, span}

class UtilsSpec extends SpecBase {
  "emptyString" should {
    "return correct value" in {
      emptyString mustBe ""
    }
  }

  "hyphen" should {
    "return correct value" in {
      hyphen mustBe "-"
    }
  }

  "singleSpace" should {
    "return correct value" in {
      singleSpace mustBe " "
    }
  }

  "period" should {
    "return correct value" in {
      period mustBe "."
    }
  }

  "pathWithQueryString" should {
    "return correct value" in {
      val path = "somePath"
      pathWithQueryString(fakeRequest("GET", path)) mustBe s"$path"
    }
  }

  "h1Component" should {
    "return the correct component type" in {
      h1Component mustBe a[h1]
    }
  }

  "h2Component" should {
    "return the correct component type" in {
      h2Component mustBe a[h2]
    }
  }

  "pComponent" should {
    "return the correct component type" in {
      pComponent mustBe a[p]
    }
  }

  "linkComponent" should {
    "return the correct component type" in {
      linkComponent mustBe a[link]
    }
  }

  "insetComponent" should {
    "return the correct component type" in {
      insetComponent mustBe a[inset]
    }
  }

  "spanComponent" should {
    "return the correct component type" in {
      spanComponent mustBe a[span]
    }
  }

  "divComponent" should {
    "return the correct component type" in {
      divComponent mustBe a[div]
    }
  }

  "ddComponent" should {
    "return the correct component type" in {
      ddComponent mustBe a[dd]
    }
  }

  "dlComponent" should {
    "return the correct component type" in {
      dlComponent mustBe a[dl]
    }
  }

  "dtComponent" should {
    "return the correct component type" in {
      dtComponent mustBe a[dt]
    }
  }

  "referrerUrl" should {
    "return correct value when platform host has some value" in {
      val path = "somePath"
      val platformHost = "localhost"
      implicit val reqHeaders: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", path)

      referrerUrl(Some(platformHost)) mustBe Option(s"$platformHost$path")
    }

    "return correct value when platform host value is empty" in {
      val path = "somePath"
      implicit val reqHeaders: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", path)

      referrerUrl(None) mustBe Option(s"$path")
    }
  }
}
