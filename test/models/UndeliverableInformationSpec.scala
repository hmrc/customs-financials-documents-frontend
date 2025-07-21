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

package models

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class UndeliverableInformationSpec extends SpecBase {
  "Json Reads" should {

    "generate correct output" in new Setup {

      import UndeliverableInformation.format

      Json.fromJson(Json.parse(undeliverinfoObJsString)) shouldBe JsSuccess(undeliverInfoOb)
    }
  }

  "Json Writes" should {

    "generate correct output" in new Setup {
      Json.toJson(undeliverInfoOb) shouldBe Json.parse(undeliverinfoObJsString)
    }
  }

  "Invalid JSON" should {
    "fail" in {
      val invalidJson = "{ \"eori\": \"testEori1\", \"eventId1\": \"thirty\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[UndeliverableInformation]
      }
    }
  }

  trait Setup {
    val undeliverInfoOb: UndeliverableInformation =
      UndeliverableInformation(subject = "test_source", eventId = "test_event", groupId = "test_group_id")

    val undeliverinfoObJsString: String =
      """{"subject":"test_source","eventId":"test_event","groupId":"test_group_id"}""".stripMargin
  }
}
