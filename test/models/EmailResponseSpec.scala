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

class EmailResponseSpec extends SpecBase {
  "Json format" should {

    "generate correct output for Json Reads" in new Setup {
      import EmailResponse.format

      Json.fromJson(Json.parse(emailResJsString)) shouldBe JsSuccess(emailResOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(emailResOb) shouldBe Json.parse(emailResJsString)
    }
  }

  trait Setup {
    val undeliverableOb: UndeliverableInformation =
      UndeliverableInformation(subject = "some-subject", eventId = "some-event-id", groupId = "some-group-id")

    val emailResOb: EmailResponse = EmailResponse(
      address = Some("some@email.com"),
      timestamp = Some("2023-12-04T16:17:25"),
      undeliverable = Some(undeliverableOb)
    )

    val emailResJsString: String =
      """{
        |"address":"some@email.com",
        |"timestamp":"2023-12-04T16:17:25",
        |"undeliverable":{"subject":"some-subject","eventId":"some-event-id","groupId":"some-group-id"}
        |}""".stripMargin
  }
}
