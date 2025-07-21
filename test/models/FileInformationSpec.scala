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

import models.metadata.Metadata
import utils.SpecBase
import utils.CommonTestData.{DOWNLOAD_URL_00, SIZE_99L, TEST_FILE_NAME}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class FileInformationSpec extends SpecBase {
  "Json Reads" should {

    "generate correct output" in new Setup {

      import FileInformation.fileInformationFormats

      Json.fromJson(Json.parse(fileInfoObJsString)) shouldBe JsSuccess(fileInfoOb)
    }
  }

  "Json Writes" should {

    "generate correct output" in new Setup {
      Json.toJson(fileInfoOb) shouldBe Json.parse(fileInfoObJsString)
    }
  }

  "Invalid JSON" should {
    "fail" in {
      val invalidJson = "{ \"filename\": \"testFile\", \"eventId1\": \"thirty\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[FileInformation]
      }
    }
  }

  trait Setup {
    val fileInfoOb: FileInformation =
      FileInformation(
        filename = TEST_FILE_NAME,
        downloadURL = DOWNLOAD_URL_00,
        fileSize = SIZE_99L,
        metadata = Metadata(Seq())
      )

    val fileInfoObJsString: String =
      """{"filename":"test_name","downloadURL":"download_url_00","fileSize":99,"metadata":[]}""".stripMargin
  }
}
