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
import utils.CommonTestData.{DAY_10, EORI_NUMBER, MONTH_1, YEAR_2023}
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import java.time.LocalDate

class EoriHistoryResponseSpec extends SpecBase {
  "Json Reads" should {

    "generate correct output" in new Setup {

      import EoriHistoryResponse.format

      Json.fromJson(Json.parse(eoriHistoryResObJsString)) shouldBe JsSuccess(eoriHistoryResOb)
    }
  }

  "Json Writes" should {

    "generate correct output" in new Setup {
      Json.toJson(eoriHistoryResOb) shouldBe Json.parse(eoriHistoryResObJsString)
    }
  }

  "Invalid JSON" should {
    "fail" in {
      val invalidJson = "{ \"eori1\": \"testEori1\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EoriHistoryResponse]
      }
    }
  }

  trait Setup {
    val eoriHistoryResOb: EoriHistoryResponse = EoriHistoryResponse(
      Seq(
        EoriHistory(
          eori = EORI_NUMBER,
          validFrom = Some(LocalDate.of(YEAR_2023, MONTH_1, DAY_10)),
          validUntil = Some(LocalDate.of(YEAR_2023, MONTH_1, DAY_10))
        )
      )
    )

    val eoriHistoryResObJsString: String =
      """{
        |"eoriHistory":[{"eori":"testEori1","validFrom":"2023-01-10","validUntil":"2023-01-10"}]
        |}""".stripMargin
  }
}
