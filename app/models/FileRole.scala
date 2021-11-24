/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.{Logger, LoggerLike}
import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import play.api.mvc.PathBindable

sealed abstract class FileRole(val name: String, val featureName: String, val transactionName: String, val messageKey: String)

object FileRole {

  case object C79Certificate extends FileRole("C79Certificate", "import-vat", "Download import VAT statement", "c79")
  case object PostponedVATStatement extends FileRole("PostponedVATStatement", "postponed-vat", "Download postponed VAT statement", "postponed-vat")
  case object SecurityStatement extends FileRole("SecurityStatement", "adjustments", "Download adjustments statement", "adjustments")
  case object PostponedVATAmendedStatement extends FileRole("PostponedVATAmendedStatement", "postponed-vat", "Download postponed VAT amend statement", "postponed-vat")

  val log: LoggerLike = Logger(this.getClass)

  def apply(name: String): FileRole = name match {
    case "C79Certificate" => C79Certificate
    case "PostponedVATStatement" => PostponedVATStatement
    case "SecurityStatement" => SecurityStatement
    case "PostponedVATAmendedStatement" => PostponedVATAmendedStatement
    case _ => throw new Exception(s"Unknown file role: $name")
  }

  def unapply(fileRole: FileRole): Option[String] = Some(fileRole.name)

  implicit val fileRoleFormat = new Format[FileRole] {
    def reads(json: JsValue) = JsSuccess(apply(json.as[String]))

    def writes(obj: FileRole) = JsString(obj.name)
  }

  implicit val pathBinder: PathBindable[FileRole] = new PathBindable[FileRole] {
    override def bind(key: String, value: String): Either[String, FileRole] = {
      value match {
        case "import-vat" => Right(C79Certificate)
        case "postponed-vat" => Right(PostponedVATStatement)
        case "adjustments" => Right(SecurityStatement)
        case fileRole => Left(s"unknown file role: ${fileRole}")
      }
    }

    override def unbind(key: String, fileRole: FileRole): String = {
      fileRole match {
        case C79Certificate => "import-vat"
        case PostponedVATStatement => "postponed-vat"
        case SecurityStatement => "adjustments"
        case _ => "unsupported-file-role"
      }
    }
  }

}