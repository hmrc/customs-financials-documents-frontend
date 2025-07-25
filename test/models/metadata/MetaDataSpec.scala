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

package models.metadata

import utils.SpecBase
import utils.SpecBase
import utils.CommonTestData.{TEST_KEY, TEST_KEY_VALUE}
import play.api.libs.json.{JsSuccess, Json}

class MetaDataSpec extends SpecBase {

  "metadataReads" should {
    "Read the object correctly" in new Setup {

      import Metadata.metadataReads

      Json.fromJson(Json.parse(metaDataJsString)) shouldBe JsSuccess(metaDataObject)
    }
  }

  "metadataWrites" should {
    "Write the object correctly" in new Setup {
      Json.toJson(metaDataObject) shouldBe Json.parse(metaDataJsString)
    }
  }

  "asMap" should {
    "return the correct map of MetaDataItems" in new Setup {
      metaDataObject.asMap shouldBe Map(TEST_KEY -> TEST_KEY_VALUE)
    }
  }

  trait Setup {
    val metaDataObject: Metadata = Metadata(Seq(MetadataItem(TEST_KEY, TEST_KEY_VALUE)))

    val metaDataJsString: String = """[{"metadata":"test_key","value":"test_value"}]""".stripMargin
  }
}
