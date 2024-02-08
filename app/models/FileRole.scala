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

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import play.api.{Logger, LoggerLike}

sealed abstract class FileRole(val name: String,
                               val featureName: String,
                               val transactionName: String,
                               val messageKey: String)

object FileRole {

  case object C79Certificate
    extends FileRole(
      name = "C79Certificate",
      featureName = "import-vat",
      transactionName = "Download import VAT statement",
      messageKey = "c79")

  case object PostponedVATStatement
    extends FileRole(
      name = "PostponedVATStatement",
      featureName = "postponed-vat",
      transactionName = "Download postponed VAT statement",
      messageKey = "postponed-vat")

  case object SecurityStatement
    extends FileRole(
      name = "SecurityStatement",
      featureName = "adjustments",
      transactionName = "Download adjustments statement",
      messageKey = "adjustments")

  case object PostponedVATAmendedStatement
    extends FileRole(
      name = "PostponedVATAmendedStatement",
      featureName = "postponed-vat",
      transactionName = "Download postponed VAT amend statement",
      messageKey = "postponed-vat")

  val log: LoggerLike = Logger(this.getClass)

  def apply(name: String): FileRole = name match {
    case "C79Certificate" => C79Certificate
    case "PostponedVATStatement" => PostponedVATStatement
    case "SecurityStatement" => SecurityStatement
    case "PostponedVATAmendedStatement" => PostponedVATAmendedStatement
    case _ => throw new Exception(s"Unknown file role: $name")
  }

  def unapply(fileRole: FileRole): Option[String] = Some(fileRole.name)

  implicit val fileRoleFormat: Format[FileRole] = new Format[FileRole] {
    def reads(json: JsValue): JsSuccess[FileRole] = JsSuccess(apply(json.as[String]))

    def writes(obj: FileRole): JsString = JsString(obj.name)
  }
}
